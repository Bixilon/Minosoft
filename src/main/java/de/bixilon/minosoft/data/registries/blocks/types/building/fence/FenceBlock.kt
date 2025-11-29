/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.blocks.types.building.fence

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.light.TransparentProperty
import de.bixilon.minosoft.data.registries.blocks.properties.list.MapPropertyList
import de.bixilon.minosoft.data.registries.blocks.properties.primitives.BooleanProperty
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.WaterloggableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.text.formatting.color.RGBArray
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.block.state.DirectBlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.models.block.state.render.NeighbourBlockRender
import de.bixilon.minosoft.gui.rendering.models.block.state.render.WorldRenderProps
import de.bixilon.minosoft.gui.rendering.models.loader.legacy.ModelChooser
import de.bixilon.minosoft.protocol.versions.Version

abstract class FenceBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), BlockWithItem<Item>, ModelChooser, OutlinedBlock, CollidableBlock, WaterloggableBlock {
    override val item: Item = this::item.inject(identifier)
    override val hardness get() = 2.0f

    override val lightProperties get() = TransparentProperty


    override fun registerProperties(version: Version, list: MapPropertyList) {
        super<Block>.registerProperties(version, list)

        if (!version.flattened) { // TODO: in flattening versions too?
            list += NORTH
            list += SOUTH
            list += WEST
            list += EAST
        }
    }

    override fun buildState(version: Version, settings: BlockStateBuilder): BlockState {
        if (version.flattened) return super<Block>.buildState(version, settings)

        return settings.build(this, collisionShape = AABB.BLOCK, outlineShape = AABB.BLOCK) // TODO: outline/collision shape (also remove in FlattenedBlockStateCodec)
    }

    override fun bakeModel(context: RenderContext, model: DirectBlockModel) {
        if (context.session.version.flattened) return super.bakeModel(context, model)

        val all = model.choose(mapOf(), true)?.bake()
        val north = model.choose(mapOf(NORTH to true), false)?.bake()
        val south = model.choose(mapOf(SOUTH to true), false)?.bake()
        val west = model.choose(mapOf(WEST to true), false)?.bake()
        val east = model.choose(mapOf(EAST to true), false)?.bake()

        this.model = FenceRenderer(all, arrayOf(north, south, west, east))
    }


    class FenceRenderer(
        val none: BlockRender?,
        val sides: Array<BlockRender?>,
    ) : NeighbourBlockRender {
        override val default: BlockRender? get() = none

        override fun render(props: WorldRenderProps, position: BlockPosition, state: BlockState, entity: BlockEntity?, tints: RGBArray?): Boolean {
            val neighbours = props.neighbours

            var rendered = false

            none?.render(props, position, state, entity, tints)?.takeIf { it }?.let { rendered = true }

            for (direction in Directions.SIDES) {
                // TODO: Only fence blocks?
                if (neighbours[direction.ordinal]?.block !is FenceBlock) continue

                sides[direction.ordinal - Directions.SIDE_OFFSET]?.render(props, position, state, entity, tints)?.takeIf { it }?.let { rendered = true }
            }

            return rendered
        }
    }


    companion object {
        val NORTH = BooleanProperty("north")
        val SOUTH = BooleanProperty("south")
        val WEST = BooleanProperty("west")
        val EAST = BooleanProperty("east")
    }
}
