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

package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.compression

import de.bixilon.kutil.buffer.ByteBufferUtil.createBuffer
import de.bixilon.kutil.buffer.arbitrary.ArbitraryByteArray
import de.bixilon.minosoft.protocol.network.network.client.netty.NetworkAllocator
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.ciritical.InvalidPacketSizeError
import de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.compression.exception.SizeMismatchInflaterException
import de.bixilon.minosoft.protocol.protocol.buffers.InByteBuffer
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import java.util.zip.Inflater


class PacketInflater(
    private val maxPacketSize: Int,
) : MessageToMessageDecoder<ArbitraryByteArray>() {

    fun decode(data: ArbitraryByteArray): ArbitraryByteArray {
        val buffer = InByteBuffer(data.array).apply { offset = data.offset }

        val length = buffer.readVarInt() // uncompressed
        val offset = buffer.offset
        val left = data.size - (offset - data.offset)

        if (length == 0) { // TODO: uncompressed if length < threshold?
            // uncompressed
            return ArbitraryByteArray(offset, left, data.array)
        }

        if (length > maxPacketSize) throw InvalidPacketSizeError(length, maxPacketSize)
        val decompressed = NetworkAllocator.allocate(length)
        val decompressedLength = data.array.decompress(decompressed, offset, left)
        NetworkAllocator.free(data.array)


        if (length != decompressedLength) throw SizeMismatchInflaterException()

        return ArbitraryByteArray(0, length, decompressed)
    }

    private fun ByteArray.decompress(output: ByteArray, offset: Int, size: Int): Int {
        val inflater = Inflater()
        inflater.setInput(this, offset, size)
        var pointer = 0
        val buffer = createBuffer()  // TODO: Buffer allocator

        while (!inflater.finished()) {
            val length = inflater.inflate(buffer)
            System.arraycopy(buffer, 0, output, pointer, length)
            pointer += length
        }
        return pointer
    }

    override fun decode(context: ChannelHandlerContext?, data: ArbitraryByteArray, out: MutableList<Any>) {
        out += decode(data)
    }

    companion object {
        const val NAME = "packet_inflater"
    }
}
