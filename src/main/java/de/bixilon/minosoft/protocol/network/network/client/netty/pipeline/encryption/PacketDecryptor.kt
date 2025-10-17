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

package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.encryption

import de.bixilon.minosoft.protocol.network.network.client.netty.NetworkAllocator
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import javax.crypto.Cipher

class PacketDecryptor(
    private val cipher: Cipher,
) : ByteToMessageDecoder() {

    // TODO: tests

    override fun decode(context: ChannelHandlerContext, data: ByteBuf, out: MutableList<Any>) {
        val length = data.readableBytes()
        val encrypted = NetworkAllocator.allocate(length) // TODO: Limit to buffer size (we can do that in small chunks to not allocate to much at once)
        data.readBytes(encrypted, 0, length)

        val decrypted = NetworkAllocator.allocate(length) // TODO: Don't double allocate
        cipher.update(encrypted, 0, length, decrypted)
        NetworkAllocator.free(encrypted)

        out += context.alloc().buffer(length).apply { writeBytes(decrypted, 0, length) }
        NetworkAllocator.free(decrypted)
    }

    companion object {
        const val NAME = "packet_decryptor"
    }
}
