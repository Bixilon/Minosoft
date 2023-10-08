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

package de.bixilon.minosoft.data.registries.blocks.types.snow

import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.legacy.FlatteningRenamedModel
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.FullOpaqueBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.shovel.ShovelRequirement
import de.bixilon.minosoft.data.registries.registries.Registries

class SnowBlock(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : Block(identifier, settings), FullOpaqueBlock, FlatteningRenamedModel, ShovelRequirement, BlockWithItem<Item>, AbstractSnowBlock {
    override val item: Item = this::item.inject(identifier)
    override val hardness get() = 0.2f
    override val legacyModelName get() = minecraft("snow")


    companion object : BlockFactory<SnowBlock> {
        override val identifier = minecraft("snow_block")

        override fun build(registries: Registries, settings: BlockSettings) = SnowBlock(settings = settings)
    }
}
