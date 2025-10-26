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
import de.bixilon.minosoft.protocol.protocol.buffers.OutByteBuffer
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater


class PacketDeflater(
    var threshold: Int,
) : MessageToMessageEncoder<ArbitraryByteArray>() {

    fun encode(data: ArbitraryByteArray): ArbitraryByteArray {
        val compress = data.size >= threshold

        if (compress) {
            val uncompressedLength = OutByteBuffer().apply { writeVarInt(data.size) }.toArray()
            val compressed = data.array.compress(data.offset, data.size) // TODO: don't double allocate
            NetworkAllocator.free(data.array)

            val size = uncompressedLength.size + compressed.size
            val final = NetworkAllocator.allocate(size)
            System.arraycopy(uncompressedLength, 0, final, 0, uncompressedLength.size)
            System.arraycopy(compressed, 0, final, uncompressedLength.size, compressed.size)

            return ArbitraryByteArray(0, size, final)
        }

        val final = NetworkAllocator.allocate(1 + data.size)
        final[0] = 0x00 // uncompressed var int length
        System.arraycopy(data.array, data.offset, final, 1, data.size)
        NetworkAllocator.free(data.array)

        return ArbitraryByteArray(0, 1 + data.size, final)
    }

    override fun encode(context: ChannelHandlerContext, data: ArbitraryByteArray, out: MutableList<Any>) {
        out += encode(data)
    }


    private fun ByteArray.compress(offset: Int, size: Int): ByteArray {
        val deflater = Deflater()
        deflater.setInput(this, offset, size)
        deflater.finish()
        val stream = ByteArrayOutputStream(this.size)

        val buffer = createBuffer(this.size)
        while (!deflater.finished()) {
            val length = deflater.deflate(buffer)
            stream.write(buffer, 0, length)
        }
        stream.close()
        return stream.toByteArray()
    }

    companion object {
        const val NAME = "packet_deflater"
    }
}
