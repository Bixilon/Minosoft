/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.network.client.pipeline.length

import de.bixilon.minosoft.protocol.network.network.client.exceptions.ciritical.PacketTooLongException
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder


class LengthDecoder(
    private val maxLength: Int,
) : ByteToMessageDecoder() {

    override fun decode(context: ChannelHandlerContext?, buffer: ByteBuf, out: MutableList<Any>) {
        buffer.markReaderIndex()
        if (buffer.readableBytes() < 2) { // 1 length byte and 1 packet id byte is the minimum
            buffer.resetReaderIndex()
            return
        }
        val length: Int
        try {
            length = buffer.readVarInt()
        } catch (error: BufferTooShortException) {
            buffer.resetReaderIndex()
            return
        }

        if (length > maxLength) {
            throw PacketTooLongException(length, maxLength)
        }

        if (buffer.readableBytes() < length) {
            buffer.resetReaderIndex()
            return
        }

        val array = ByteArray(length)
        buffer.readBytes(array)

        out += array
    }


    companion object {
        const val NAME = "length_decoder"

        private fun ByteBuf.readVarInt(): Int {
            var readCount = 0
            var varInt = 0
            var currentByte: Int
            do {
                if (this.readableBytes() <= 0) {
                    throw BufferTooShortException()
                }
                currentByte = this.readByte().toInt()
                val value = currentByte and 0x7F
                varInt = varInt or (value shl 7 * readCount)
                readCount++
                if (readCount > 5) {
                    throw RuntimeException("VarInt is too big")
                }
            } while (currentByte and 0x80 != 0)

            return varInt
        }

        private class BufferTooShortException : Exception()
    }
}
