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

package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.compression

import de.bixilon.kutil.compression.zlib.ZlibUtil.compress
import de.bixilon.minosoft.protocol.protocol.buffers.OutByteBuffer
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder


class PacketDeflater(
    var threshold: Int,
) : MessageToMessageEncoder<ByteArray>() {

    override fun encode(context: ChannelHandlerContext, data: ByteArray, out: MutableList<Any>) {
        val compress = data.size >= threshold

        val prefixed = OutByteBuffer()
        if (compress) {
            val compressed = data.compress()
            prefixed.writeVarInt(data.size)
            prefixed.writeBareByteArray(compressed)
        } else {
            prefixed.writeVarInt(0)
            prefixed.writeBareByteArray(data)
        }

        out += prefixed.toArray()
    }

    companion object {
        const val NAME = "packet_deflater"
    }
}
