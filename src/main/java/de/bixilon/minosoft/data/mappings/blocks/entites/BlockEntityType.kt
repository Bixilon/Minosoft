/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings.blocks.entites

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.block.BlockEntityFactory
import de.bixilon.minosoft.data.entities.block.DefaultBlockEntityMetaDataFactory
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.types.Block
import de.bixilon.minosoft.data.mappings.registry.RegistryItem
import de.bixilon.minosoft.data.mappings.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.mappings.versions.Registries
import de.bixilon.minosoft.protocol.network.connection.PlayConnection

data class BlockEntityType(
    override val resourceLocation: ResourceLocation,
    private var blockIds: Set<Int>?,
    val factory: BlockEntityFactory<out BlockEntity>,
) : RegistryItem {
    lateinit var blocks: Set<Block>
        private set

    override fun postInit(registries: Registries) {
        val blocks: MutableSet<Block> = mutableSetOf()

        for (blockId in blockIds!!) {
            blocks += registries.blockRegistry[blockId]
        }
        this.blockIds = null

        this.blocks = blocks.toSet()
    }

    fun build(connection: PlayConnection): BlockEntity {
        return DefaultBlockEntityMetaDataFactory.buildBlockEntity(factory, connection)
    }

    companion object : ResourceLocationDeserializer<BlockEntityType> {
        override fun deserialize(mappings: Registries?, resourceLocation: ResourceLocation, data: JsonObject): BlockEntityType? {
            val factory = DefaultBlockEntityMetaDataFactory.getEntityFactory(resourceLocation) ?: return null // ToDo

            val blockIds: MutableSet<Int> = mutableSetOf()

            for (blockId in data["blocks"].asJsonArray) {
                blockIds += blockId.asInt
            }

            return BlockEntityType(
                resourceLocation = resourceLocation,
                blockIds = blockIds,
                factory = factory,
            )
        }
    }
}
