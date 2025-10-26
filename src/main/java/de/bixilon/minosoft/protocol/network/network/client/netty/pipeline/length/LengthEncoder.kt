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

import de.bixilon.kutil.buffer.arbitrary.ArbitraryByteArray
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.ciritical.InvalidPacketSizeError
import de.bixilon.minosoft.protocol.protocol.buffers.OutByteBuffer
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder


class LengthEncoder(
    private val maxLength: Int,
) : MessageToByteEncoder<ArbitraryByteArray>() {

    fun write(data: ArbitraryByteArray, out: ByteBuf) {
        if (data.size > maxLength) {
            throw InvalidPacketSizeError(data.size, maxLength)
        }
        val length = OutByteBuffer().apply { writeVarInt(data.size) }.toArray()
        out.writeBytes(length)
        out.writeBytes(data.array, data.offset, data.size)
    }

    override fun encode(context: ChannelHandlerContext?, data: ArbitraryByteArray, out: ByteBuf) {
        write(data, out)
    }

    companion object {
        const val NAME = "length_encoder"
    }
}
