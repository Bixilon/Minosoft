/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.protocol.buffers

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.nbt.tag.NBTTagTypes
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.nbtType

open class OutByteBuffer : de.bixilon.kutil.buffer.bytes.out.OutByteBuffer {

    constructor() : super()
    constructor(buffer: OutByteBuffer) : super(buffer)

    fun writeJson(json: Any) {
        writeString(Jackson.MAPPER.writeValueAsString(json))
    }

    fun writeChatComponent(chatComponent: ChatComponent) {
        writeJson(chatComponent.getJson())
    }


    // TODO kutil 1.19.2
    /*
    override fun writeString(string: String) {
        check(string.length <= ProtocolDefinition.STRING_MAX_LENGTH) { "String max string length exceeded ${string.length} > ${ProtocolDefinition.STRING_MAX_LENGTH}" }
        val bytes = string.encodeNetwork()
        writeVarInt(bytes.size)
        writeBareByteArray(bytes)
    }

     */

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
                writeBareByteArray(tag)
            }

            is CharSequence -> {
                val bytes = tag.toString().toByteArray(Charsets.UTF_8)
                if (bytes.size > Short.MAX_VALUE * 2) {
                    error("String exceeds max length!")
                }
                writeShort(bytes.size)
                writeBareByteArray(bytes)
            }

            is Collection<*> -> {
                this.writeNBTTagType(
                    if (tag.isEmpty()) {
                        NBTTagTypes.END
                    } else {
                        tag.iterator().next().nbtType
                    }
                )

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
                writeBareIntArray(tag)
            }

            is LongArray -> {
                writeInt(tag.size)
                writeBareLongArray(tag)
            }
        }
    }

    fun writeByteBlockPosition(blockPosition: Vec3i?) {
        writeInt(blockPosition?.x ?: 0)
        writeByte(blockPosition?.y ?: 0)
        writeInt(blockPosition?.z ?: 0)
    }

    fun writeLegacyResourceLocation(resourceLocation: ResourceLocation) {
        if (resourceLocation.namespace == Namespaces.DEFAULT) {
            writeString(resourceLocation.path)
            return
        }
        writeResourceLocation(resourceLocation)
    }

    fun writeResourceLocation(resourceLocation: ResourceLocation) {
        writeString(resourceLocation.toString())
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
        writeFloat(vec3.x)
        writeFloat(vec3.y)
        writeFloat(vec3.z)
    }
}
