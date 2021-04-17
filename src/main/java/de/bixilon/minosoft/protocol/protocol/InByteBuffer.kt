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
import com.google.gson.JsonParser
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.commands.CommandArgumentNode
import de.bixilon.minosoft.data.commands.CommandLiteralNode
import de.bixilon.minosoft.data.commands.CommandNode
import de.bixilon.minosoft.data.commands.CommandRootNode
import de.bixilon.minosoft.data.entities.Poses
import de.bixilon.minosoft.data.mappings.LegacyResourceLocation
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.nbt.tag.*
import de.bixilon.minosoft.util.nbt.tag.NBTTagTypes.Companion.VALUES
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*


open class InByteBuffer {
    private val connection: Connection?
    private val bytes: ByteArray
    var pointer = 0

    constructor(bytes: ByteArray, connection: Connection) {
        this.bytes = bytes
        this.connection = connection
    }

    constructor(buffer: InByteBuffer) {
        connection = buffer.connection
        bytes = buffer.bytes.clone()
        pointer = buffer.pointer
    }

    val size: Int
        get() = bytes.size

    val bytesLeft: Int
        get() = size - pointer

    inline fun <reified T> readArray(length: Int = readVarInt(), reader: () -> T): Array<T> {
        check(length <= size) { "Trying to allocate to much memory!" }
        val array: MutableList<T> = mutableListOf()
        for (i in 0 until length) {
            array.add(i, reader.invoke())
        }
        return array.toTypedArray()
    }

    fun readByte(): Byte {
        return bytes[pointer++]
    }

    fun readByteArray(length: Int = readVarInt()): ByteArray {
        check(length <= bytes.size) { "Trying to allocate to much memory!" }
        val array = ByteArray(length)
        for (i in 0 until length) {
            array[i] = readByte()
        }
        return array
    }

    fun readUnsignedByte(): Int {
        return readByte().toInt() and ((1 shl Byte.SIZE_BITS) - 1)
    }


    fun readShort(): Short {
        return (readUnsignedByte() shl Byte.SIZE_BITS or readUnsignedByte()).toShort()
    }

    fun readShortArray(length: Int = readVarInt()): ShortArray {
        check(length <= bytes.size / Short.SIZE_BYTES) { "Trying to allocate to much memory!" }
        val array = ShortArray(length)
        for (i in 0 until length) {
            array[i] = readShort()
        }
        return array
    }

    fun readUnsignedShort(): Int {
        return readShort().toInt() and ((1 shl Short.SIZE_BITS) - 1)
    }


    fun readInt(): Int {
        return (readUnsignedShort() shl Short.SIZE_BITS or readUnsignedShort())
    }

    fun readIntArray(length: Int = readVarInt()): IntArray {
        check(length <= bytes.size / Int.SIZE_BYTES) { "Trying to allocate to much memory!" }
        val array = IntArray(length)
        for (i in 0 until length) {
            array[i] = readInt()
        }
        return array
    }

    fun readUnsignedInt(): Long {
        return readInt().toLong() and ((1L shl Int.SIZE_BITS) - 1)
    }


    fun readVarInt(): Int {
        var byteCount = 0
        var result = 0
        var read: Int
        do {
            read = readUnsignedByte()
            result = result or (read and 0x7F shl (Byte.SIZE_BITS - 1) * byteCount)
            byteCount++
            require(byteCount <= Int.SIZE_BYTES + 1) { "VarInt is too big" }
        } while (read and 0x80 != 0)

        return result
    }

    @Deprecated(message = "Legacy only", replaceWith = ReplaceWith("readVarIntArray(readVarInt())"))
    fun readVarIntArray(): IntArray {
        return readVarIntArray(readVarInt())
    }

    fun readVarIntArray(length: Int = readVarInt()): IntArray {
        check(length <= bytes.size) { "Trying to allocate to much memory!" }
        val array = IntArray(length)
        for (i in 0 until length) {
            array[i] = readVarInt()
        }
        return array
    }

    fun readUnsignedVarInt(): Long {
        return readVarInt().toLong() and ((1 shl Int.SIZE_BITS) - 1).toLong()
    }


    fun readLong(): Long {
        return (readUnsignedInt() shl Int.SIZE_BITS or readUnsignedInt())
    }

    fun readLongArray(length: Int = readVarInt()): LongArray {
        check(length <= bytes.size / Long.SIZE_BYTES) { "Trying to allocate to much memory!" }
        val array = LongArray(length)
        for (i in 0 until length) {
            array[i] = readLong()
        }
        return array
    }


    fun readVarLong(): Long {
        var byteCount = 0
        var result = 0L
        var read: Int
        do {
            read = readUnsignedByte()
            result = result or ((read and 0x7F shl (Byte.SIZE_BITS - 1) * byteCount).toLong())
            byteCount++
            require(byteCount <= Long.SIZE_BYTES + 1) { "VarLong is too big" }
        } while (read and 0x80 != 0)

        return result
    }

    fun readVarLongArray(length: Int = readVarInt()): LongArray {
        check(length <= bytes.size) { "Trying to allocate to much memory!" }
        val array = LongArray(length)
        for (i in 0 until length) {
            array[i] = readVarLong()
        }
        return array
    }

    fun readFloat(): Float {
        return Float.fromBits(readInt())
    }

    fun readFloatArray(length: Int = readVarInt()): FloatArray {
        check(length <= bytes.size / Float.SIZE_BYTES) { "Trying to allocate to much memory!" }
        val array = FloatArray(length)
        for (i in 0 until length) {
            array[i] = readFloat()
        }
        return array
    }


    fun readDouble(): Double {
        return Double.fromBits(readLong())
    }

    fun readDoubleArray(length: Int = readVarInt()): DoubleArray {
        check(length <= bytes.size / Double.SIZE_BYTES) { "Trying to allocate to much memory!" }
        val array = DoubleArray(length)
        for (i in 0 until length) {
            array[i] = readDouble()
        }
        return array
    }


    fun readFixedPointNumberInt(): Double {
        return readInt() / 32.0
    }

    fun readFixedPointNumberByte(): Double {
        return readByte() / 32.0
    }

    fun readBoolean(): Boolean {
        return readUnsignedByte() == 1
    }

    @Deprecated(message = "Java legacy", replaceWith = ReplaceWith("readString()"))
    fun readString(): String {
        return readString(readVarInt())
    }

    fun readString(length: Int = readVarInt()): String {
        val string = String(readByteArray(length), StandardCharsets.UTF_8)
        check(string.length <= ProtocolDefinition.STRING_MAX_LENGTH) { "String max string length exceeded ${string.length} > ${ProtocolDefinition.STRING_MAX_LENGTH}" }
        return string
    }

    fun readStringArray(length: Int = readVarInt()): Array<String> {
        return readArray(length) { readString() }
    }

    fun readUUID(): UUID {
        return UUID(readLong(), readLong())
    }

    fun readUUIDArray(length: Int = readVarInt()): Array<UUID> {
        return readArray(length) { readUUID() }
    }

    fun readJson(): JsonObject {
        return JsonParser.parseString(readString()).asJsonObject
    }

    fun readJsonArray(length: Int = readVarInt()): Array<JsonObject> {
        return readArray(length) { readJson() }
    }

    open fun readChatComponent(): ChatComponent {
        return ChatComponent.valueOf(raw = readString())
    }

    fun readChatComponentArray(length: Int = readVarInt()): Array<ChatComponent> {
        return readArray(length) { readChatComponent() }
    }

    fun readDirection(): Directions {
        return Directions.DIRECTIONS[readVarInt()]
    }

    fun readPose(): Poses {
        return Poses.byId(readVarInt())
    }

    fun readRest(): ByteArray {
        return readByteArray(length = size - pointer)
    }

    fun readAngle(): Int {
        return (readByte() * ProtocolDefinition.ANGLE_CALCULATION_CONSTANT).toInt()
    }

    fun readPosition(): Vec3 {
        return Vec3(readDouble(), readDouble(), readDouble())
    }

    fun readFloatPosition(): Vec3 {
        return Vec3(readFloat(), readFloat(), readFloat())
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
        val resourceLocation = readString()

        return if (Util.doesStringContainsUppercaseLetters(resourceLocation)) {
            // just a string but wrapped into a resourceLocation (like old plugin channels MC|BRAND or ...)
            LegacyResourceLocation(resourceLocation)
        } else {
            ResourceLocation(resourceLocation)
        }
    }


    fun readCommandNode(): CommandNode {
        val flags = readByte().toInt()
        return when (CommandNode.NodeTypes.byId(flags and 0x03)!!) {
            CommandNode.NodeTypes.ROOT -> CommandRootNode(flags, this)
            CommandNode.NodeTypes.LITERAL -> CommandLiteralNode(flags, this)
            CommandNode.NodeTypes.ARGUMENT -> CommandArgumentNode(flags, this)
        }
    }

    @Deprecated(message = "Legacy only", replaceWith = ReplaceWith("readCommandNodeArray(readVarInt())"))
    fun readCommandNodeArray(): Array<CommandNode> {
        return readCommandNodeArray(readVarInt())
    }

    fun readCommandNodeArray(length: Int = readVarInt()): Array<CommandNode> {
        val nodes = readArray(length) { readCommandNode() }
        for (node in nodes) {
            if (node.redirectNodeId != -1) {
                node.redirectNode = nodes[node.redirectNodeId]
            }

            for (childId in node.childrenIds) {
                when (val child = nodes[childId]) {
                    is CommandArgumentNode -> {
                        node.argumentsChildren.add(child)
                    }
                    is CommandLiteralNode -> {
                        node.literalChildren[child.name] = child
                    }
                }
            }
        }


        return nodes
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

    @Deprecated(message = "Refactored soon!")
    fun readNBT(tagType: NBTTagTypes): NBTTag? {
        return when (tagType) {
            NBTTagTypes.END -> null
            NBTTagTypes.BYTE -> ByteTag(this)
            NBTTagTypes.SHORT -> ShortTag(this)
            NBTTagTypes.INT -> IntTag(this)
            NBTTagTypes.LONG -> LongTag(this)
            NBTTagTypes.FLOAT -> FloatTag(this)
            NBTTagTypes.DOUBLE -> DoubleTag(this)
            NBTTagTypes.BYTE_ARRAY -> ByteArrayTag(this)
            NBTTagTypes.STRING -> StringTag(this)
            NBTTagTypes.LIST -> ListTag(this)
            NBTTagTypes.COMPOUND -> CompoundTag(true, this)
            NBTTagTypes.INT_ARRAY -> IntArrayTag(this)
            NBTTagTypes.LONG_ARRAY -> LongArrayTag(this)
        }
    }

    @Deprecated(message = "Refactored soon!")
    fun readNBT(compressed: Boolean): NBTTag? {
        if (compressed) {
            val length = readUnsignedShort()
            return if (length == -1) {
                // no nbt data here...
                CompoundTag()
            } else try {
                InByteBuffer(Util.decompressGzip(readByteArray(length)), connection!!).readNBT(false)
            } catch (e: IOException) {
                // oh no
                e.printStackTrace()
                throw IllegalArgumentException("Bad nbt")
            }
        }
        val type = VALUES[readUnsignedByte()]
        return if (type === NBTTagTypes.COMPOUND) {
            // shouldn't be a subtag
            CompoundTag(false, this)
        } else {
            readNBT(type)
        }
    }

    fun getBase64(): String {
        return String(Base64.getEncoder().encode(readRest()))
    }

}
