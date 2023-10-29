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

package de.bixilon.minosoft.data.registries.blocks.types.entity.storage

import de.bixilon.minosoft.data.entities.block.container.storage.ChestBlockEntity
import de.bixilon.minosoft.data.entities.block.container.storage.TrappedChestBlockEntity
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.axe.AxeRequirement
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class WoodenChestBlock<T : ChestBlockEntity>(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), BlockWithItem<Item>, AxeRequirement, DoubleChestBlock<T> {
    override val hardness: Float get() = 2.5f
    override val item: Item = this::item.inject(identifier)


    open class Chest(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoodenChestBlock<ChestBlockEntity>(identifier, settings) {

        override fun createBlockEntity(connection: PlayConnection) = ChestBlockEntity(connection)

        companion object : BlockFactory<Chest> {
            override val identifier = minecraft("chest")

            override fun build(registries: Registries, settings: BlockSettings) = Chest(settings = settings)
        }
    }

    open class TrappedChest(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : WoodenChestBlock<TrappedChestBlockEntity>(identifier, settings) {

        override fun createBlockEntity(connection: PlayConnection) = TrappedChestBlockEntity(connection)

        companion object : BlockFactory<TrappedChest> {
            override val identifier = minecraft("trapped_chest")

            override fun build(registries: Registries, settings: BlockSettings) = TrappedChest(settings = settings)
        }
    }
}
