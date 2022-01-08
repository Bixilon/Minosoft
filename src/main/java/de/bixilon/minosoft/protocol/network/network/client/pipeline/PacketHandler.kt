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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.StatusS2CPacket
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class PacketHandler(
    private val connection: Connection,
) : SimpleChannelInboundHandler<S2CPacket>() {

    override fun channelRead0(context: ChannelHandlerContext, packet: S2CPacket) {
        if (packet is PlayS2CPacket) {
            val connection = connection.unsafeCast<PlayConnection>()
            packet.log(connection.profiles.other.log.reducedProtocolLog)
            packet.check(connection)
            packet.handle(connection)
        } else if (packet is StatusS2CPacket) {
            packet.log(OtherProfileManager.selected.log.reducedProtocolLog)
            packet.check(connection.unsafeCast())
            packet.handle(connection.unsafeCast())
        }
    }

    override fun exceptionCaught(context: ChannelHandlerContext, cause: Throwable) {
        context.close()
        cause.printStackTrace()
    }

    companion object {
        const val NAME = "packet_handler"
    }
}
