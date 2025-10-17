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

package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.length

import de.bixilon.kutil.exception.FastException
import de.bixilon.minosoft.protocol.network.network.client.netty.NetworkAllocator
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.ciritical.InvalidPacketSizeError
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder


class LengthDecoder(
    private val maxLength: Int,
) : ByteToMessageDecoder() {

    private fun readLength(buffer: ByteBuf): Int {
        if (buffer.readableBytes() < 2) { // 1 length byte and 1 packet id byte is the minimum
            return -1
        }
        val length: Int
        try {
            length = buffer.readVarInt()
        } catch (error: BufferTooShortException) {
            return -1
        }

        if (length <= 0 || length > maxLength) {
            throw InvalidPacketSizeError(length, maxLength)
        }

        if (buffer.readableBytes() < length) {
            return -1
        }

        return length
    }

    fun read(buffer: ByteBuf): LengthDecodedPacket? {
        buffer.markReaderIndex()
        val length = readLength(buffer)
        if (length < 0) {
            buffer.resetReaderIndex()
            return null
        }

        val array = NetworkAllocator.allocate(length)
        buffer.readBytes(array, 0, length)

        return LengthDecodedPacket(0, length, array)
    }

    override fun decode(context: ChannelHandlerContext?, buffer: ByteBuf, out: MutableList<Any>) {
        out += read(buffer) ?: return
    }


    companion object {
        const val NAME = "length_decoder"

        private fun ByteBuf.readVarInt(): Int {
            var count = 0
            var data = 0
            var current: Int
            do {
                if (this.readableBytes() <= 0) {
                    throw BufferTooShortException()
                }
                current = this.readByte().toInt()
                val value = current and 0x7F
                data = data or (value shl 7 * count)
                count++
                if (count > 5) {
                    throw IllegalStateException("VarInt is too big")
                }
            } while (current and 0x80 != 0)


            return data
        }

        private class BufferTooShortException : FastException()
    }
}
