/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kmath.vec.vec2.d.Vec2d
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import de.bixilon.kutil.buffer.arbitrary.ArbitraryByteArray
import de.bixilon.kutil.compression.zlib.GzipUtil.decompress
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.config.DebugOptions
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTTagTypes
import java.nio.charset.StandardCharsets


open class InByteBuffer : de.bixilon.kutil.buffer.bytes.`in`.InByteBuffer {

    constructor(data: ArbitraryByteArray) : super(data)
    constructor(data: ByteArray) : super(data)

    fun readFixedPointNumberInt(): Double {
        return readInt() / 32.0
    }

    fun readFixedPointNumberByte(): Double {
        return readByte() / 32.0
    }

    override fun readString(length: Int): String {
        val string = String(readByteArray(length), StandardCharsets.UTF_8)
        check(string.length <= ProtocolDefinition.STRING_MAX_LENGTH) { "String max string length exceeded ${string.length} > ${ProtocolDefinition.STRING_MAX_LENGTH}" }
        return string
    }

    fun readJson(): Map<String, Any> {
        return Jackson.MAPPER.readValue(readString(), Jackson.JSON_MAP_TYPE)
    }

    open fun readChatComponent(): ChatComponent {
        val string = readString()
        if (DebugOptions.LOG_RAW_CHAT) {
            Log.log(LogMessageType.CHAT_IN, LogLevels.VERBOSE) { TextComponent(string) }
        }
        return ChatComponent.of(string, null, restricted = true)
    }

    fun readDirection(): Directions {
        return Directions.VALUES[readVarInt()]
    }

    fun readPose(): Poses {
        return Poses[readVarInt()]
    }

    fun readAngle(): Float {
        return (readByte() * ProtocolDefinition.ROTATION_ANGLE_DIVIDER)
    }

    fun readEntityRotation(): EntityRotation {
        return EntityRotation(readAngle(), readAngle())
    }

    fun readVec2f(): Vec2f {
        return Vec2f(readFloat(), readFloat())
    }

    fun readVec2d(): Vec2d {
        return Vec2d(readDouble(), readDouble())
    }

    fun readVec3f(): Vec3f {
        return Vec3f(readFloat(), readFloat(), readFloat())
    }

    open fun readVec3d(): Vec3d {
        return Vec3d(readDouble(), readDouble(), readDouble())
    }

    fun readVec4f(): Vec4f {
        return Vec4f(readFloat(), readFloat(), readFloat(), readFloat())
    }


    fun readByteBlockPosition(): BlockPosition {
        return BlockPosition(readInt(), readUnsignedByte(), readInt())
    }

    fun readShortBlockPosition(): BlockPosition {
        return BlockPosition(readInt(), readShort().toInt(), readInt())
    }

    fun readIntBlockPosition(): BlockPosition {
        return BlockPosition(readInt(), readInt(), readInt())
    }

    fun readResourceLocation(): ResourceLocation {
        return ResourceLocation.of(readString())
    }

    fun readChunkPosition(): ChunkPosition {
        return ChunkPosition(readInt(), readInt())
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

    fun readNBTTag(compressed: Boolean, named: Boolean): Any? {
        if (compressed) {
            val length = readShort().toInt()
            return if (length == -1) {
                // no nbt data here...
                null
            } else {
                InByteBuffer(readByteArray(length).decompress()).readNBTTag(false, named)
            }
        }
        val type = NBTTagTypes[readUnsignedByte()]
        if (type === NBTTagTypes.COMPOUND && named) {
            var name = readString(readUnsignedShort()) // ToDo: Should this name be ignored?
        }
        return readNBTTag(type)
    }

    fun <T : Enum<*>> readEnum(universe: ValuesEnum<T>): T {
        val ordinal = readVarInt()
        return universe[ordinal]
    }
}
