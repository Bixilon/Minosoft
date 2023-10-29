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

import de.bixilon.minosoft.data.entities.block.container.storage.EnderChestBlockEntity
import de.bixilon.minosoft.data.registries.blocks.entites.BlockEntityType
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.tool.pickaxe.PickaxeRequirement
import de.bixilon.minosoft.data.registries.registries.Registries

open class EnderChestBlock(identifier: ResourceLocation = this.identifier, settings: BlockSettings) : Block(identifier, settings), ChestBlock<EnderChestBlockEntity>, PickaxeRequirement {
    override val blockEntity: BlockEntityType<EnderChestBlockEntity> = this::blockEntity.inject(this)
    override val hardness: Float get() = 22.5f

    companion object : BlockFactory<EnderChestBlock> {
        override val identifier = minecraft("ender_chest")

        override fun build(registries: Registries, settings: BlockSettings) = EnderChestBlock(settings = settings)
    }
}
