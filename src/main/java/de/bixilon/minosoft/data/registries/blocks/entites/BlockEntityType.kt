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

package de.bixilon.minosoft.data.registries.blocks.entites

import de.bixilon.kutil.cast.CollectionCast.asAnyCollection
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.block.BlockEntityFactory
import de.bixilon.minosoft.data.entities.block.DefaultBlockDataFactory
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.codec.ResourceLocationCodec
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

data class BlockEntityType<T : BlockEntity>(
    override val identifier: ResourceLocation,
    val blocks: Set<Block>,
    val factory: BlockEntityFactory<T>,
) : RegistryItem() {

    fun build(connection: PlayConnection): T {
        return factory.build(connection)
    }

    companion object : ResourceLocationCodec<BlockEntityType<*>> {
        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): BlockEntityType<*>? {
            // ToDo: Fix resource location
            check(registries != null)
            val factory = DefaultBlockDataFactory[resourceLocation] ?: return null // ToDo

            val blocks: MutableSet<Block> = mutableSetOf()

            for (block in data["blocks"].asAnyCollection()) {
                blocks += registries.blockRegistry[block] ?: continue
            }

            return BlockEntityType(
                identifier = resourceLocation,
                blocks = blocks,
                factory = factory,
            )
        }
    }
}
