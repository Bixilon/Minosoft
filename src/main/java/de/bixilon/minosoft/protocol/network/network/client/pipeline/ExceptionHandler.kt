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

package de.bixilon.minosoft.protocol.network.network.client.pipeline

import de.bixilon.minosoft.protocol.network.network.client.NettyClient
import de.bixilon.minosoft.protocol.network.network.client.exceptions.NetworkException
import de.bixilon.minosoft.protocol.network.network.client.exceptions.ciritical.CriticalNetworkException
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext

class ExceptionHandler(
    private val client: NettyClient,
) : ChannelDuplexHandler() {

    override fun exceptionCaught(context: ChannelHandlerContext, cause: Throwable) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.WARN) { cause }
        if (cause !is NetworkException) {
            client.disconnect()
            return
        }
        if (cause is CriticalNetworkException) {
            client.disconnect()
            return
        }
    }

    companion object {
        const val NAME = "exception_handler"
    }
}
