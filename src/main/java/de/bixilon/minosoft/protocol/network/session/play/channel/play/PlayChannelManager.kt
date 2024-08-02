/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.session.play.channel.play

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedList
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.NetworkConnection
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.channel.ChannelManager
import de.bixilon.minosoft.protocol.packets.c2s.common.ChannelC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer

class PlayChannelManager(
    private val session: PlaySession,
) : ChannelManager<PlayChannelHandler>() {


    fun handle(channel: ResourceLocation, data: ByteArray) {
        val handlers = handlers[channel] ?: return

        for (handler in handlers.toSynchronizedList()) { // ToDo: properly lock
            val buffer = PlayInByteBuffer(data, session)
            try {
                handler.handle(buffer)
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }
    }

    fun send(channel: ResourceLocation, message: PlayOutByteBuffer) {
        send(channel, message.toArray())
    }

    fun send(channel: ResourceLocation, data: ByteArray) {
        // TODO: Play == ready? what if offline? should not crash
        if (session.connection.nullCast<NetworkConnection>()?.state != ProtocolStates.PLAY) {
            throw IllegalStateException("Not in play!")
        }
        session.connection.send(ChannelC2SP(channel, data))
    }
}
