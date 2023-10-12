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

package de.bixilon.minosoft.data.registries.blocks.types.wood

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.light.CustomLightProperties
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.WaterloggableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.LightedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.CustomDiggingBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.FullBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.properties.requirement.ToolRequirement
import de.bixilon.minosoft.data.registries.item.items.tool.shears.ShearsItem
import de.bixilon.minosoft.data.registries.item.items.tool.sword.SwordItem
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.CustomBlockCulling
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.gui.rendering.tint.TintedBlock
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class LeavesBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), CustomBlockCulling, FullBlock, BlockStateBuilder, ToolRequirement, CustomDiggingBlock, WaterloggableBlock, BlockWithItem<Item>, LightedBlock, TintedBlock {
    override val hardness get() = 0.2f
    override val item: Item = this::item.inject(identifier)
    override val tintProvider: TintProvider? = null

    override fun initTint(manager: TintManager) {
        this::tintProvider.forceSet(manager.foliageTintCalculator)
    }

    override fun buildState(settings: BlockStateSettings): BlockState {
        return PropertyBlockState(this, settings)
    }

    override fun getLightProperties(blockState: BlockState) = LIGHT_PROPERTIES

    override fun shouldCull(state: BlockState, properties: FaceProperties, directions: Directions, neighbour: BlockState): Boolean {
        return neighbour.block != this
    }

    override fun isCorrectTool(item: Item): Boolean {
        return item is SwordItem || item is ShearsItem
    }

    override fun getMiningSpeed(connection: PlayConnection, state: BlockState, stack: ItemStack, speed: Float): Float {
        if (stack.item.item is ShearsItem) {
            return 15.0f
        }

        return speed
    }

    companion object {
        val LIGHT_PROPERTIES = CustomLightProperties(true, false, true)
    }
}
