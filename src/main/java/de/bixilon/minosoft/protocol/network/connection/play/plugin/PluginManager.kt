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

package de.bixilon.minosoft.protocol.network.connection.play.plugin

import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedList
import de.bixilon.kutil.collections.map.SynchronizedMap
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.modding.event.events.PluginMessageReceiveEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer

class PluginManager(val connection: PlayConnection) {
    private val handlers: SynchronizedMap<ResourceLocation, MutableSet<PluginHandler>> = synchronizedMapOf()


    init {
        connection.register(CallbackEventListener.of<PluginMessageReceiveEvent> { handleMessage(it.channel, it.data.readRest()) })
    }

    private fun handleMessage(channel: ResourceLocation, data: ByteArray) {
        val handlers = handlers[channel] ?: return
        for (handler in handlers.toSynchronizedList()) { // ToDo: properly lock
            val buffer = PlayInByteBuffer(data, connection)
            handler.handle(buffer)
        }
    }

    fun register(channel: ResourceLocation, handler: PluginHandler) {
        handlers.synchronizedGetOrPut(channel) { synchronizedSetOf() } += handler
    }

    operator fun set(channel: ResourceLocation, handler: PluginHandler) = register(channel, handler)
}
