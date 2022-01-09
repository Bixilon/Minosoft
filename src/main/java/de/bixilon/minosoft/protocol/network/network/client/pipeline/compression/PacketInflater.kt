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

package de.bixilon.minosoft.protocol.network.network.client.pipeline.compression

import de.bixilon.minosoft.protocol.protocol.OutByteBuffer
import de.bixilon.minosoft.util.KUtil.compressZlib
import de.bixilon.minosoft.util.KUtil.withLengthPrefix
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder


class PacketInflater(
    var threshold: Int,
) : MessageToMessageEncoder<ByteArray>() {

    override fun encode(context: ChannelHandlerContext, data: ByteArray, out: MutableList<Any>) {
        val compress = data.size >= threshold

        if (!compress) {
            val prefixed = OutByteBuffer()
            prefixed.writeVarInt(0)
            prefixed.writeUnprefixedByteArray(data)
            out += prefixed.toArray()
            return
        }

        val compressed = data.compressZlib()

        out += compressed.withLengthPrefix()
    }

    companion object {
        const val NAME = "packet_inflater"
    }
}
