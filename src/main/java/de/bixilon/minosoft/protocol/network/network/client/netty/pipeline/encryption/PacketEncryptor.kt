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

package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.encryption

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import javax.crypto.Cipher

class PacketEncryptor(
    private val cipher: Cipher,
) : MessageToByteEncoder<ByteBuf>() {

    override fun encode(context: ChannelHandlerContext, data: ByteBuf, out: ByteBuf) {
        val dataArray = ByteArray(data.readableBytes())
        data.readBytes(dataArray)

        out.writeBytes(cipher.update(dataArray))
    }

    companion object {
        const val NAME = "packet_encryptor"
    }
}
