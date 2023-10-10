/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.connection.play.channel.vanila

import de.bixilon.minosoft.data.registries.fallback.FallbackRegistries
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.modding.channels.DefaultPluginChannels
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.channel.play.PlayChannelHandler
import de.bixilon.minosoft.protocol.packets.c2s.common.ChannelC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer

object BrandHandler {
    const val VANILLA = "vanilla"
    const val MINOSOFT = "minosoft"

    fun register(connection: PlayConnection) {
        connection.channels.play[connection.getBrandChannel()] = BrandChannelHandler(connection)
    }

    private fun PlayConnection.getBrandChannel(): ResourceLocation {
        return FallbackRegistries.DEFAULT_PLUGIN_CHANNELS_REGISTRY.forVersion(version)[DefaultPluginChannels.BRAND]!!.name
    }

    private fun PlayConnection.sendBrand(channel: ResourceLocation, brand: String) {
        val buffer = PlayOutByteBuffer(this)
        buffer.writeBareByteArray(brand.encodeNetwork())
        sendPacket(ChannelC2SP(channel, buffer))
    }

    fun PlayConnection.sendBrand() {
        sendBrand(getBrandChannel(), if (profiles.connection.fakeBrand) VANILLA else MINOSOFT)
    }


    private class BrandChannelHandler(
        private val connection: PlayConnection,
    ) : PlayChannelHandler {

        override fun handle(buffer: PlayInByteBuffer) {
            connection.serverInfo.brand = buffer.readString(if (buffer.versionId >= ProtocolDefinition.FLATTENING_VERSION) buffer.readVarInt() else buffer.bytesLeft)
        }
    }
}
