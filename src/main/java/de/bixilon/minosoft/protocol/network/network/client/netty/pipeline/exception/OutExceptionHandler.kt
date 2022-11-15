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

package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.exception

import de.bixilon.minosoft.protocol.network.network.client.netty.NettyClient
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise

class OutExceptionHandler(
    private val client: NettyClient,
) : ChannelOutboundHandlerAdapter() {

    override fun write(context: ChannelHandlerContext, message: Any, promise: ChannelPromise) {
        promise.addListener {
            if (!it.isSuccess) {
                client.handleError(it.cause())
            }
        }
        super.write(context, message, promise)
    }

    companion object {
        const val NAME = "out_exception_handler"
    }
}
