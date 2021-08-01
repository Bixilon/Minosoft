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

package de.bixilon.minosoft.data.registries.blocks.entites

import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.block.BlockEntityFactory
import de.bixilon.minosoft.data.entities.block.DefaultBlockEntityMetaDataFactory
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.listCast

data class BlockEntityType(
    override val resourceLocation: ResourceLocation,
    val blocks: Set<Block>,
    val factory: BlockEntityFactory<out BlockEntity>,
) : RegistryItem() {

    fun build(connection: PlayConnection): BlockEntity {
        return DefaultBlockEntityMetaDataFactory.buildBlockEntity(factory, connection)
    }

    companion object : ResourceLocationDeserializer<BlockEntityType> {
        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): BlockEntityType? {
            check(registries != null)
            val factory = DefaultBlockEntityMetaDataFactory[resourceLocation] ?: return null // ToDo

            val blocks: MutableSet<Block> = mutableSetOf()

            for (block in data["blocks"]?.listCast()!!) {
                blocks += registries.blockRegistry[block] ?: continue
            }

            return BlockEntityType(
                resourceLocation = resourceLocation,
                blocks = blocks,
                factory = factory,
            )
        }
    }
}
