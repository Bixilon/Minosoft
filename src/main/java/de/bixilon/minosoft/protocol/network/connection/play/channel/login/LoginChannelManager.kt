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

package de.bixilon.minosoft.protocol.network.connection.play.channel.login

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedList
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.channel.ChannelManager
import de.bixilon.minosoft.protocol.packets.c2s.login.ChannelC2SP
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolStates

class LoginChannelManager(
    private val connection: PlayConnection,
) : ChannelManager<LoginChannelHandler>() {


    fun handle(messageId: Int, channel: ResourceLocation, data: ByteArray) {
        val handlers = handlers[channel] ?: return reject(messageId)

        var handled = 0
        for (handler in handlers.toSynchronizedList()) { // ToDo: properly lock
            val buffer = PlayInByteBuffer(data, connection)
            try {
                handler.handle(messageId, buffer)
                handled++
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }
        if (handled == 0) {
            reject(messageId)
        }
    }

    private fun reject(messageId: Int) {
        send(messageId, null)
    }

    fun send(messageId: Int, data: OutByteBuffer) {
        send(messageId, data.toArray())
    }

    fun send(messageId: Int, data: ByteArray?) {
        if (connection.network.state != ProtocolStates.LOGIN) {
            throw IllegalStateException("Not in login!")
        }
        connection.sendPacket(ChannelC2SP(messageId, data))
    }
}
