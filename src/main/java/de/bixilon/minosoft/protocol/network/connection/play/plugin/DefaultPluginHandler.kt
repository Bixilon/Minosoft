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

import de.bixilon.minosoft.data.registries.DefaultRegistries
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.modding.channels.DefaultPluginChannels
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.PluginC2SP
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer

object DefaultPluginHandler {

    fun register(connection: PlayConnection) {
        registerBrand(connection)
    }

    private fun registerBrand(connection: PlayConnection) {
        val brandChannel = DefaultRegistries.DEFAULT_PLUGIN_CHANNELS_REGISTRY.forVersion(connection.version)[DefaultPluginChannels.BRAND]!!.resourceLocation
        connection.pluginManager[brandChannel] = {
            connection.serverInfo.brand = it.readString()

            sendBrand(brandChannel, connection) // ToDo: Option to set brand
        }
    }

    private fun sendBrand(channel: ResourceLocation, connection: PlayConnection, brand: String = "vanilla") {
        val buffer = PlayOutByteBuffer(connection)
        buffer.writeString(brand) // ToDo: Remove length prefix
        connection.sendPacket(PluginC2SP(channel, buffer))
    }
}
