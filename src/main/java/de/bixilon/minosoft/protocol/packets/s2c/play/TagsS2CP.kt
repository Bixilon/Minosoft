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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.registry.Registry
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.tags.MinecraftTagTypes.BLOCK
import de.bixilon.minosoft.tags.MinecraftTagTypes.ENTITY_TYPE
import de.bixilon.minosoft.tags.MinecraftTagTypes.FLUID
import de.bixilon.minosoft.tags.MinecraftTagTypes.GAME_EVENT
import de.bixilon.minosoft.tags.MinecraftTagTypes.ITEM
import de.bixilon.minosoft.tags.Tag
import de.bixilon.minosoft.tags.TagList
import de.bixilon.minosoft.tags.TagManager
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class TagsS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val tags: TagManager

    init {
        val tags: MutableMap<ResourceLocation, TagList<*>> = mutableMapOf()
        if (buffer.versionId < ProtocolVersions.V_20W51A) {
            tags[BLOCK] = buffer.readBlockTags()
            tags[ITEM] = buffer.readItemTags()
            tags[FLUID] = buffer.readFluidTags() // ToDo: when was this added? Was not available in 18w01
            if (buffer.versionId >= ProtocolVersions.V_18W43A) {
                tags[ENTITY_TYPE] = buffer.readEntityTypeTags()
            }
            if (buffer.versionId >= ProtocolVersions.V_20W49A) {
                tags[GAME_EVENT] = buffer.readGameEventTags()
            }
        } else {
            for (i in 0 until buffer.readVarInt()) {
                val resourceLocation = buffer.readResourceLocation()
                tags[resourceLocation] = when (resourceLocation) {
                    BLOCK -> buffer.readBlockTags()
                    ITEM -> buffer.readItemTags()
                    FLUID -> buffer.readFluidTags()
                    ENTITY_TYPE -> buffer.readEntityTypeTags()
                    GAME_EVENT -> buffer.readGameEventTags()
                    else -> buffer.readTagList(Registry())
                }
            }
        }
        this.tags = TagManager(tags.unsafeCast())
    }

    private fun PlayInByteBuffer.readBlockTags(): TagList<Block> {
        return readTagList(connection.registries.block)
    }

    private fun PlayInByteBuffer.readItemTags(): TagList<Item> {
        return readTagList(connection.registries.item)
    }

    private fun PlayInByteBuffer.readFluidTags(): TagList<Fluid> {
        return readTagList(connection.registries.fluid)
    }

    private fun PlayInByteBuffer.readEntityTypeTags(): TagList<EntityType> {
        return readTagList(connection.registries.entityType)
    }

    @Deprecated("TODO: Game events")
    private fun PlayInByteBuffer.readGameEventTags(): TagList<*> {
        return readTagList(Registry())
    }

    private fun <T : RegistryItem> PlayInByteBuffer.readTag(registry: Registry<T>): Tag<T> {
        val items: MutableSet<T> = mutableSetOf()
        for (id in readVarIntArray()) {
            items += registry.getOrNull(id) ?: continue
        }
        return Tag(items)
    }


    private fun <T : RegistryItem> PlayInByteBuffer.readTagList(registry: Registry<T>): TagList<T> {
        val entries: MutableMap<ResourceLocation, Tag<T>> = mutableMapOf()
        for (index in 0 until readVarInt()) {
            val key = readResourceLocation()
            val tag = readTag(registry)
            entries[key] = tag
        }
        return TagList(entries)
    }

    override fun handle(connection: PlayConnection) {
        connection.tags = tags
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Tags (tags=$tags)" }
    }
}
