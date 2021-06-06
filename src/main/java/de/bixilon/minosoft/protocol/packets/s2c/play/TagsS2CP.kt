/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.tags.Tag
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class TagsS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val tags: Map<ResourceLocation, Map<ResourceLocation, Tag<Any>>>

    init {
        val tags: MutableMap<ResourceLocation, Map<ResourceLocation, Tag<Any>>> = mutableMapOf()
        if (buffer.versionId < ProtocolVersions.V_20W51A) {
            tags[BLOCK_TAG_RESOURCE_LOCATION] = buffer.readTagArray { buffer.connection.registries.blockRegistry[it] }
            tags[ITEM_TAG_RESOURCE_LOCATION] = buffer.readTagArray { buffer.connection.registries.itemRegistry[it] }
            tags[FLUID_TAG_RESOURCE_LOCATION] = buffer.readTagArray { buffer.connection.registries.fluidRegistry[it] } // ToDo: when was this added? Was not available in 18w01
            if (buffer.versionId >= ProtocolVersions.V_18W43A) {
                tags[ENTITY_TYPE_TAG_RESOURCE_LOCATION] = buffer.readTagArray { buffer.connection.registries.entityTypeRegistry[it] }
            }
            if (buffer.versionId >= ProtocolVersions.V_20W49A) {
                tags[GAME_EVENT_TAG_RESOURCE_LOCATION] = buffer.readTagArray { it }
            }
        } else {
            for (i in 0 until buffer.readVarInt()) {
                val resourceLocation = buffer.readResourceLocation()
                tags[resourceLocation] = when (resourceLocation) {
                    BLOCK_TAG_RESOURCE_LOCATION -> buffer.readTagArray { buffer.connection.registries.blockRegistry[it] }
                    ITEM_TAG_RESOURCE_LOCATION -> buffer.readTagArray { buffer.connection.registries.itemRegistry[it] }
                    FLUID_TAG_RESOURCE_LOCATION -> buffer.readTagArray { buffer.connection.registries.fluidRegistry[it] }
                    ENTITY_TYPE_TAG_RESOURCE_LOCATION -> buffer.readTagArray { buffer.connection.registries.entityTypeRegistry[it] }
                    // GAME_EVENT_TAG_RESOURCE_LOCATION -> buffer.readTagArray { it }
                    else -> buffer.readTagArray { it }
                }
            }
        }
        this.tags = tags.toMap()
    }

    override fun handle(connection: PlayConnection) {
        connection.tags.putAll(tags)
    }

    override fun log() {
        if (Minosoft.config.config.general.reduceProtocolLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Tags (tags=$tags)" }
    }

    companion object {
        val BLOCK_TAG_RESOURCE_LOCATION = "minecraft:block".asResourceLocation()
        val ITEM_TAG_RESOURCE_LOCATION = "minecraft:item".asResourceLocation()
        val FLUID_TAG_RESOURCE_LOCATION = "minecraft:fluid".asResourceLocation()
        val ENTITY_TYPE_TAG_RESOURCE_LOCATION = "minecraft:entity_type".asResourceLocation()
        val GAME_EVENT_TAG_RESOURCE_LOCATION = "minecraft:game_event".asResourceLocation()
    }
}
