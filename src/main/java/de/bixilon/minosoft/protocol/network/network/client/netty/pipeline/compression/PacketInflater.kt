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

import de.bixilon.kutil.compression.zlib.ZlibUtil.decompress
import de.bixilon.minosoft.protocol.network.network.client.netty.NetworkAllocator
import de.bixilon.minosoft.protocol.network.network.client.netty.ReadArray
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.ciritical.PacketTooLongException
import de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.compression.exception.SizeMismatchInflaterException
import de.bixilon.minosoft.protocol.protocol.buffers.InByteBuffer
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder


class PacketInflater(
    private val maxPacketSize: Int,
) : MessageToMessageDecoder<ReadArray>() {

    override fun decode(context: ChannelHandlerContext?, data: ReadArray, out: MutableList<Any>) {
        val buffer = InByteBuffer(data.array)

        val uncompressedLength = buffer.readVarInt()
        val length = data.length - buffer.pointer
        val compressed = NetworkAllocator.allocate(length)
        buffer.readByteArray(compressed, 0, length)
        if (uncompressedLength == 0) {
            out += ReadArray(compressed, length)
            return
        }
        if (uncompressedLength > maxPacketSize) {
            throw PacketTooLongException(uncompressedLength, maxPacketSize)
        }

        val decompressed = NetworkAllocator.allocate(uncompressedLength)

        val actualDecompressed = compressed.decompress(decompressed)
        NetworkAllocator.free(compressed)

        if (actualDecompressed != uncompressedLength) {
            throw SizeMismatchInflaterException()
        }
        out += ReadArray(decompressed, uncompressedLength)
    }

    companion object {
        const val NAME = "packet_inflater"
    }
}
