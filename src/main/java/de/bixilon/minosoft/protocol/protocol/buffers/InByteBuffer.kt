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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.compression.zlib.GzipUtil.decompress
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.tags.Tag
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.nbt.tag.NBTTagTypes


open class InByteBuffer : de.bixilon.kutil.buffer.bytes.`in`.InByteBuffer {

    constructor(bytes: ByteArray) : super(bytes)
    constructor(buffer: InByteBuffer) : super(buffer)


    fun readFixedPointNumberInt(): Double {
        return readInt() / 32.0
    }

    fun readFixedPointNumberByte(): Double {
        return readByte() / 32.0
    }

    // TODO kutil 1.19.2
    /*
    override fun readString(length: Int = readVarInt()): String {
        val string = String(readByteArray(length), StandardCharsets.UTF_8)
        check(string.length <= ProtocolDefinition.STRING_MAX_LENGTH) { "String max string length exceeded ${string.length} > ${ProtocolDefinition.STRING_MAX_LENGTH}" }
        return string
    }
     */

    fun readJson(): Map<String, Any> {
        return Jackson.MAPPER.readValue(readString(), Jackson.JSON_MAP_TYPE)
    }

    open fun readChatComponent(): ChatComponent {
        return ChatComponent.of(readString(), restrictedMode = true)
    }

    fun readDirection(): Directions {
        return Directions.VALUES[readVarInt()]
    }

    fun readPose(): Poses {
        return Poses[readVarInt()]
    }

    fun readAngle(): Int {
        return (readByte() * ProtocolDefinition.ROTATION_ANGLE_DIVIDER).toInt()
    }

    fun readEntityRotation(): EntityRotation {
        return EntityRotation(readAngle().toFloat(), readAngle().toFloat())
    }

    fun readVec2f(): Vec2 {
        return Vec2(readFloat(), readFloat())
    }

    fun readVec2d(): Vec2d {
        return Vec2d(readDouble(), readDouble())
    }

    fun readVec3f(): Vec3 {
        return Vec3(readFloat(), readFloat(), readFloat())
    }

    open fun readVec3d(): Vec3d {
        return Vec3d(readDouble(), readDouble(), readDouble())
    }

    fun readByteBlockPosition(): Vec3i {
        return Vec3i(readInt(), readByte(), readInt())
    }

    fun readShortBlockPosition(): Vec3i {
        return Vec3i(readInt(), readShort(), readInt())
    }

    fun readIntBlockPosition(): Vec3i {
        return Vec3i(readInt(), readInt(), readInt())
    }

    fun readResourceLocation(): ResourceLocation {
        return ResourceLocation.of(readString())
    }

    fun readChunkPosition(): Vec2i {
        return Vec2i(readInt(), readInt())
    }

    fun readUnsignedShortsLE(length: Int): IntArray {
        require(length <= size) { "Trying to allocate to much memory" }
        val ret = IntArray(length)
        for (i in 0 until length) {
            ret[i] = readUnsignedByte() or (readUnsignedByte() shl 8)
        }
        return ret
    }

    fun readNBTTag(tagType: NBTTagTypes): Any? {
        return when (tagType) {
            NBTTagTypes.END -> null
            NBTTagTypes.BYTE -> readByte()
            NBTTagTypes.SHORT -> readShort()
            NBTTagTypes.INT -> readInt()
            NBTTagTypes.LONG -> readLong()
            NBTTagTypes.FLOAT -> readFloat()
            NBTTagTypes.DOUBLE -> readDouble()
            NBTTagTypes.BYTE_ARRAY -> readByteArray(readInt())
            NBTTagTypes.STRING -> readString(readUnsignedShort())
            NBTTagTypes.LIST -> {
                val listType = NBTTagTypes[readUnsignedByte()]
                val length = readInt()
                val out: MutableList<Any> = mutableListOf()
                for (i in 0 until length) {
                    readNBTTag(listType)?.let { out.add(it) }
                }
                out
            }

            NBTTagTypes.COMPOUND -> {
                val out: MutableMap<String, Any> = mutableMapOf()
                while (true) {
                    val compoundTagType = NBTTagTypes[readUnsignedByte()]
                    if (compoundTagType === NBTTagTypes.END) {
                        // end tag
                        break
                    }
                    val tagName: String = readString(readUnsignedShort())
                    val tag = readNBTTag(compoundTagType) ?: continue
                    out[tagName] = tag
                }
                out
            }

            NBTTagTypes.INT_ARRAY -> readIntArray(readInt())
            NBTTagTypes.LONG_ARRAY -> readLongArray(readInt())
        }
    }

    fun readNBTTag(compressed: Boolean): Any? {
        if (compressed) {
            val length = readShort().toInt()
            return if (length == -1) {
                // no nbt data here...
                null
            } else {
                InByteBuffer(readByteArray(length).decompress()).readNBTTag(false)
            }
        }
        val type = NBTTagTypes[readUnsignedByte()]
        if (type === NBTTagTypes.COMPOUND) {
            var name = readString(readUnsignedShort()) // ToDo: Should this name be ignored?
        }
        return readNBTTag(type)
    }

    fun <T> readTag(idResolver: (Int) -> T): Pair<ResourceLocation, Tag<T>> {
        val resourceLocation = readResourceLocation()
        val ids = readVarIntArray()
        val items: MutableSet<T> = mutableSetOf()
        for (id in ids) {
            items += idResolver(id)
        }
        return Pair(resourceLocation, Tag(items))
    }
}
