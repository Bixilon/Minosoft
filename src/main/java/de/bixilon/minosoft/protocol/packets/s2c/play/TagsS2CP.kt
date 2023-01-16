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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.tags.Tag
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class TagsS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val tags: Map<ResourceLocation, Map<ResourceLocation, Tag<Any>>>

    init {
        val tags: MutableMap<ResourceLocation, Map<ResourceLocation, Tag<Any>>> = mutableMapOf()
        if (buffer.versionId < ProtocolVersions.V_20W51A) {
            tags[BLOCK_TAG_RESOURCE_LOCATION] = mapOf(*(buffer.readArray { buffer.readTag { buffer.connection.registries.block[it] } }))
            tags[ITEM_TAG_RESOURCE_LOCATION] = mapOf(*(buffer.readArray { buffer.readTag { buffer.connection.registries.item[it] } }))
            tags[FLUID_TAG_RESOURCE_LOCATION] = mapOf(*(buffer.readArray { buffer.readTag { buffer.connection.registries.fluid[it] } })) // ToDo: when was this added? Was not available in 18w01
            if (buffer.versionId >= ProtocolVersions.V_18W43A) {
                tags[ENTITY_TYPE_TAG_RESOURCE_LOCATION] = mapOf(*(buffer.readArray { buffer.readTag { buffer.connection.registries.entityType[it] } }))
            }
            if (buffer.versionId >= ProtocolVersions.V_20W49A) {
                tags[GAME_EVENT_TAG_RESOURCE_LOCATION] = mapOf(*(buffer.readArray { buffer.readTag { it } }))
            }
        } else {
            for (i in 0 until buffer.readVarInt()) {
                val resourceLocation = buffer.readResourceLocation()
                tags[resourceLocation] = when (resourceLocation) {
                    BLOCK_TAG_RESOURCE_LOCATION -> mapOf(*(buffer.readArray { buffer.readTag { buffer.connection.registries.block[it] } }))
                    ITEM_TAG_RESOURCE_LOCATION -> mapOf(*(buffer.readArray { buffer.readTag { buffer.connection.registries.item[it] } }))
                    FLUID_TAG_RESOURCE_LOCATION -> mapOf(*(buffer.readArray { buffer.readTag { buffer.connection.registries.fluid[it] } }))
                    ENTITY_TYPE_TAG_RESOURCE_LOCATION -> mapOf(*(buffer.readArray { buffer.readTag { buffer.connection.registries.entityType[it] } }))
                    // GAME_EVENT_TAG_RESOURCE_LOCATION -> buffer.readTagArray { it }
                    else -> mapOf(*(buffer.readArray { buffer.readTag { it } }))
                }
            }
        }
        this.tags = tags
    }

    override fun handle(connection: PlayConnection) {
        connection.tags.putAll(tags)
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Tags (tags=$tags)" }
    }

    companion object {
        val BLOCK_TAG_RESOURCE_LOCATION = "minecraft:block".toResourceLocation()
        val ITEM_TAG_RESOURCE_LOCATION = "minecraft:item".toResourceLocation()
        val FLUID_TAG_RESOURCE_LOCATION = "minecraft:fluid".toResourceLocation()
        val ENTITY_TYPE_TAG_RESOURCE_LOCATION = "minecraft:entity_type".toResourceLocation()
        val GAME_EVENT_TAG_RESOURCE_LOCATION = "minecraft:game_event".toResourceLocation()
    }
}
