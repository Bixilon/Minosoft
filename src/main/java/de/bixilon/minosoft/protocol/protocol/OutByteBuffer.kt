/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.protocol

import com.google.gson.JsonObject
import com.sun.javafx.geom.Vec3f
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.collections.bytes.HeapArrayByteList
import de.bixilon.minosoft.util.nbt.tag.NBTTagTypes
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.nbtType
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*

open class OutByteBuffer() {
    private val bytes = HeapArrayByteList()

    constructor(buffer: OutByteBuffer) : this() {
        bytes.addAll(buffer.bytes)
    }

    fun writeShort(short: Short) {
        writeShort(short.toInt())
    }

    fun writeShort(short: Int) {
        writeByte(short ushr Byte.SIZE_BITS)
        writeByte(short)
    }

    fun writeInt(int: Int) {
        writeShort(int shr Short.SIZE_BITS)
        writeShort(int)
    }

    open fun writeUnprefixedByteArray(data: ByteArray) {
        bytes.addAll(data)
    }

    open fun writeByteArray(data: ByteArray) {
        writeVarInt(data.size)
        bytes.addAll(data)
    }

    fun writeLong(value: Long) {
        writeInt((value shr Int.SIZE_BITS).toInt())
        writeInt(value.toInt())
    }

    fun writeChatComponent(chatComponent: ChatComponent) {
        writeString(chatComponent.legacyText)
    }

    fun writeJSON(json: JsonObject) {
        writeString(Util.GSON.toJson(json))
    }

    fun writeString(string: String) {
        check(string.length <= ProtocolDefinition.STRING_MAX_LENGTH) { "String max string length exceeded ${string.length} > ${ProtocolDefinition.STRING_MAX_LENGTH}" }
        val bytes = string.toByteArray(StandardCharsets.UTF_8)
        writeVarInt(bytes.size)
        writeUnprefixedByteArray(bytes)
    }

    fun writeVarLong(long: Long) {
        var value = long
        do {
            var temp = value and 0x7F
            value = value ushr 7
            if (value != 0L) {
                temp = temp or 0x80
            }
            writeByte(temp)
        } while (value != 0L)
    }

    fun writeByte(byte: Byte) {
        bytes.add(byte)
    }

    fun writeByte(byte: Int) {
        writeByte((byte and 0xFF).toByte())
    }

    fun writeByte(long: Long) {
        writeByte((long and 0xFF).toByte())
    }

    fun writeFloat(float: Float) {
        writeInt(float.toBits())
    }

    fun writeFloat(float: Double) {
        writeFloat(float.toFloat())
    }

    fun writeDouble(double: Double) {
        writeLong(double.toBits())
    }

    fun writeDouble(float: Float) {
        writeDouble(float.toDouble())
    }

    fun writeUUID(uuid: UUID) {
        writeLong(uuid.mostSignificantBits)
        writeLong(uuid.leastSignificantBits)
    }

    fun writeFixedPointNumberInt(double: Double) {
        writeInt((double * 32.0).toInt())
    }

    fun writeVarInt(int: Int) {
        // thanks https://wiki.vg/Protocol#VarInt_and_VarLong
        var value = int
        do {
            var temp = value and 0x7F
            value = value ushr 7
            if (value != 0) {
                temp = temp or 0x80
            }
            writeByte(temp)
        } while (value != 0)
    }

    protected fun writeNBTTagType(type: NBTTagTypes) {
        writeByte(type.ordinal)
    }

    fun writeNBT(nbt: Any?, compressed: Boolean = false) {
        if (compressed) {
            TODO("Can not write compressed NBT yet!")
        }
        if (nbt is Map<*, *>) {
            if (nbt.isEmpty()) {
                return writeNBTTag(null)
            }
            writeNBTTagType(NBTTagTypes.COMPOUND)
            writeShort(0) // Length of compound tag name
            writeNBTTag(nbt, false)
            return
        }
        writeNBTTag(nbt)
    }

    fun writeNBTTag(tag: Any?, writeType: Boolean = true) {
        fun writeNBTTagType(type: NBTTagTypes) {
            if (!writeType) {
                return
            }
            this.writeNBTTagType(type)
        }

        val type = tag.nbtType
        writeNBTTagType(type)
        if (type == NBTTagTypes.END) {
            return
        }
        when (tag) {
            is Byte -> writeByte(tag)
            is Short -> writeShort(tag)
            is Int -> writeInt(tag)
            is Long -> writeLong(tag)
            is Float -> writeFloat(tag)
            is Double -> writeDouble(tag)
            is ByteArray -> {
                writeInt(tag.size)
                writeUnprefixedByteArray(tag)
            }
            is CharSequence -> {
                val bytes = tag.toString().toByteArray(Charsets.UTF_8)
                if (bytes.size > Short.MAX_VALUE * 2) {
                    error("String exceeds max length!")
                }
                writeShort(bytes.size)
                writeUnprefixedByteArray(bytes)
            }
            is Collection<*> -> {
                this.writeNBTTagType(if (tag.isEmpty()) {
                    NBTTagTypes.END
                } else {
                    tag.iterator().next().nbtType
                })

                writeInt(tag.size)

                for (element in tag) {
                    writeNBTTag(element, false)
                }
            }
            is Map<*, *> -> {
                for ((key, value) in tag) {
                    val valueType = value.nbtType
                    if (valueType == NBTTagTypes.END) {
                        error("NBT does not support null as value in a compound tag!")
                    }
                    this.writeNBTTagType(valueType)
                    writeNBTTag(key?.toString() ?: "", false)
                    writeNBTTag(value, false)
                }
                this.writeNBTTagType(NBTTagTypes.END)
            }
            is IntArray -> {
                writeInt(tag.size)
                writeUnprefixedIntArray(tag)
            }
            is LongArray -> {
                writeInt(tag.size)
                writeUnprefixedLongArray(tag)
            }
        }
    }

    fun writeBoolean(value: Boolean) {
        writeByte(if (value) {
            0x01
        } else {
            0x00
        })
    }

    fun writeUnprefixedString(string: String) {
        writeUnprefixedByteArray(string.toByteArray(StandardCharsets.UTF_8))
    }

    fun writeByteBlockPosition(blockPosition: Vec3i?) {
        writeInt(blockPosition?.x ?: 0)
        writeByte(blockPosition?.y ?: 0)
        writeInt(blockPosition?.z ?: 0)
    }

    fun toArray(): ByteArray {
        return bytes.toArray()
    }

    fun writeUnprefixedIntArray(data: IntArray) {
        for (i in data) {
            writeInt(i)
        }
    }

    fun writeIntArray(data: IntArray) {
        writeVarInt(data.size)
        writeUnprefixedIntArray(data)
    }

    fun writeUnprefixedLongArray(data: LongArray) {
        for (l in data) {
            writeLong(l)
        }
    }

    fun writeLongArray(data: LongArray) {
        writeVarInt(data.size)
        writeUnprefixedLongArray(data)
    }

    fun writeTo(buffer: ByteBuffer) {
        buffer.put(toArray())
    }

    fun writeResourceLocation(resourceLocation: ResourceLocation) {
        writeString(resourceLocation.full)
    }

    fun writeVec3d(vec3: Vec3) {
        writeVec3d(Vec3d(vec3))
    }

    fun writeVec3d(vec3: Vec3d) {
        writeDouble(vec3.x)
        writeDouble(vec3.y)
        writeDouble(vec3.z)
    }

    fun writeVec3f(vec3: Vec3) {
        writeVec3f(Vec3f(vec3.x, vec3.y, vec3.z))
    }

    fun writeVec3f(vec3: Vec3f) {
        writeFloat(vec3.x)
        writeFloat(vec3.y)
        writeFloat(vec3.z)
    }

    fun <T> writeArray(array: Array<T>, writer: (T) -> Unit) {
        writeVarInt(array.size)
        for (entry in array) {
            writer(entry)
        }
    }

    fun <T> writeArray(collection: Collection<T>, writer: (T) -> Unit) {
        writeVarInt(collection.size)
        for (entry in collection) {
            writer(entry)
        }
    }
}
