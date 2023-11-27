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

package de.bixilon.minosoft.data.registries.blocks.types.building

import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.FullOpaqueBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.pickaxe.PickaxeRequirement
import de.bixilon.minosoft.protocol.versions.Version

abstract class RockBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), PickaxeRequirement, FullOpaqueBlock, BlockStateBuilder, BlockWithItem<Item> {
    override val item: Item = this::item.inject(identifier)

    override fun buildState(version: Version, settings: BlockStateSettings): BlockState {
        return BlockState(this, settings.luminance)
    }
}
