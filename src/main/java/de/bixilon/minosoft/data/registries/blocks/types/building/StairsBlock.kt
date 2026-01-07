/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
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

import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names
import de.bixilon.minosoft.data.direction.DirectionUtil.rotateY
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.properties.EnumProperty
import de.bixilon.minosoft.data.registries.blocks.properties.Halves
import de.bixilon.minosoft.data.registries.blocks.properties.list.MapPropertyList
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
import de.bixilon.minosoft.data.registries.item.items.tool.axe.AxeRequirement
import de.bixilon.minosoft.data.registries.item.items.tool.pickaxe.PickaxeRequirement
import de.bixilon.minosoft.data.registries.item.items.tool.properties.requirement.HandBreakable
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

abstract class StairsBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), BlockWithItem<Item>, ModelChooser, OutlinedBlock, CollidableBlock, WaterloggableBlock {
    override val item: Item = this::item.inject(identifier)

    // TODO: Collision/Outline shape

    override fun registerProperties(version: Version, list: MapPropertyList) {
        super<Block>.registerProperties(version, list)
        list += FACING
        list += HALF
        list += SHAPE
    }

    override fun buildState(version: Version, settings: BlockStateBuilder): BlockState {
        if (version.flattened) return super<Block>.buildState(version, settings)

        return settings.build(this, collisionShape = AABB.BLOCK, outlineShape = AABB.BLOCK) // TODO: outline/collision shape (also remove in BlockStateBuilder)
    }


    override fun bakeModel(context: RenderContext, model: DirectBlockModel) {
        if (context.session.version.flattened) return super.bakeModel(context, model)


        for (state in this.states) {
            val models = arrayOf(
                model.choose(state.properties + mapOf(SHAPE to Shapes.STRAIGHT), false)?.bake(),

                model.choose(state.properties + mapOf(SHAPE to Shapes.INNER_LEFT), false)?.bake(),
                model.choose(state.properties + mapOf(SHAPE to Shapes.INNER_RIGHT), false)?.bake(),

                model.choose(state.properties + mapOf(SHAPE to Shapes.OUTER_LEFT), false)?.bake(),
                model.choose(state.properties + mapOf(SHAPE to Shapes.OUTER_RIGHT), false)?.bake()
            )

            state.model = StairsRenderer(models)
        }
    }

    private fun getShape(state: BlockState, a: BlockState?, b: BlockState?, map: Array<Shapes>): Shapes? {
        if (a == null || a.block !is StairsBlock) return null
        val half = state[HALF]
        if (a[HALF] != half) return null

        val facing = state[FACING]

        if (a[FACING].axis == facing.axis) return null

        if (b != null && b.block is StairsBlock && b[FACING] == facing && b[HALF] == half) {
            return null
        }

        if (a[FACING] == facing.rotateY(-1)) return map[ShapeDirection.LEFT.ordinal]

        return map[ShapeDirection.RIGHT.ordinal]
    }


    fun getShape(state: BlockState, neighbours: Array<BlockState?>): Shapes {
        val facing = state[FACING]
        val a = neighbours[facing.ordinal]
        val b = neighbours[facing.inverted.ordinal]
        val shape = getShape(state, a, b, ShapeDirection.OUTER) ?: getShape(state, b, a, ShapeDirection.INNER) ?: Shapes.STRAIGHT

        return shape
    }

    inner class StairsRenderer(
        val models: Array<BlockRender?>,
    ) : NeighbourBlockRender {
        override val default: BlockRender? get() = models[Shapes.STRAIGHT.ordinal]

        init {
            assert(models.size == Shapes.VALUES.size)
        }


        override fun render(props: WorldRenderProps, position: BlockPosition, state: BlockState, entity: BlockEntity?, tints: RGBArray?): Boolean {
            val model = models[getShape(state, props.neighbours).ordinal]
            return model?.render(props, position, state, entity, tints) ?: false
        }
    }


    abstract class Wooden(identifier: ResourceLocation, settings: BlockSettings) : StairsBlock(identifier, settings), HandBreakable, AxeRequirement {
        override val hardness get() = 2.0f
    }

    abstract class Stone(identifier: ResourceLocation, settings: BlockSettings) : StairsBlock(identifier, settings), PickaxeRequirement {
        override val hardness get() = 1.5f
    }


    enum class ShapeDirection {
        LEFT,
        RIGHT,
        ;

        companion object {
            val INNER = arrayOf(Shapes.INNER_LEFT, Shapes.INNER_RIGHT)
            val OUTER = arrayOf(Shapes.OUTER_LEFT, Shapes.OUTER_RIGHT)
        }
    }

    enum class Shapes {
        STRAIGHT,

        INNER_LEFT,
        INNER_RIGHT,

        OUTER_LEFT,
        OUTER_RIGHT,
        ;

        companion object : ValuesEnum<Shapes> {
            override val VALUES = values()
            override val NAME_MAP = names()
        }
    }

    // TODO: deepslate, copper, end stone, brick, quartz, stones, purpur, sandstone, mud brick


    companion object {
        val FACING = EnumProperty("facing", Directions, Directions.set(Directions.NORTH, Directions.SOUTH, Directions.WEST, Directions.EAST))
        val HALF = EnumProperty("half", Halves, Halves.set(Halves.UPPER, Halves.LOWER))
        val SHAPE = EnumProperty("shape", Shapes)
    }
}
