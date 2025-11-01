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

package de.bixilon.minosoft.data.registries.blocks.types.building.door

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.camera.target.targets.BlockTarget
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.direction.DirectionUtil.rotateY
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.light.TransparentProperty
import de.bixilon.minosoft.data.registries.blocks.properties.*
import de.bixilon.minosoft.data.registries.blocks.properties.list.MapPropertyList
import de.bixilon.minosoft.data.registries.blocks.properties.primitives.BooleanProperty
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.CollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.AdvancedBlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateBuilder
import de.bixilon.minosoft.data.registries.blocks.state.builder.BlockStateSettings
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.InteractBlockHandler
import de.bixilon.minosoft.data.registries.blocks.types.properties.LightedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.size.DoubleSizeBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.tool.axe.AxeRequirement
import de.bixilon.minosoft.data.registries.item.items.tool.pickaxe.PickaxeRequirement
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.update.block.ChunkLocalBlockUpdate
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.block.state.DirectBlockModel
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.models.block.state.render.PickedBlockRender
import de.bixilon.minosoft.gui.rendering.models.loader.legacy.ModelChooser
import de.bixilon.minosoft.input.interaction.InteractionResults
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.versions.Version

abstract class DoorBlock(identifier: ResourceLocation, settings: BlockSettings) : Block(identifier, settings), BlockWithItem<Item>, ModelChooser, DoubleSizeBlock, InteractBlockHandler, OutlinedBlock, CollidableBlock, BlockStateBuilder, LightedBlock {
    override val item: Item = this::item.inject(identifier)

    override fun getLightProperties(blockState: BlockState) = TransparentProperty

    override fun register(version: Version, list: MapPropertyList) {
        super<Block>.register(version, list)
        list += HALF; list += HINGE; list += POWERED; list += FACING; list += OPEN
    }

    override fun buildState(version: Version, settings: BlockStateSettings): BlockState {
        if (!version.flattened) return PropertyBlockState(this, settings)

        val hinge = settings.properties!![HINGE].unsafeCast<Sides>()
        val open = settings.properties[OPEN].toBoolean()
        val facing = settings.properties[FACING].unsafeCast<Directions>()
        val shape = getShape(hinge, open, facing)

        return AdvancedBlockState(this, settings.properties, 0, shape, shape, settings.lightProperties)
    }

    private fun legacyCycleOpen(chunk: Chunk, inChunk: InChunkPosition, state: BlockState) {
        val next = state.withProperties(OPEN to !state[OPEN])

        chunk[inChunk] = next
    }

    fun cycleOpen(session: PlaySession, position: BlockPosition, state: BlockState) {
        // TODO: move that to DoubleSizeBlock?

        val chunk = session.world.chunks[position.chunkPosition] ?: return

        val inChunk = position.inChunkPosition
        val top = isTop(state, session)

        val otherPosition = inChunk + if (top) Directions.DOWN else Directions.UP
        val otherState = chunk[otherPosition] ?: return
        if (otherState.block !is DoorBlock) return


        if (!session.version.flattened) {
            legacyCycleOpen(chunk, if (top) otherPosition else inChunk, if (top) otherState else state)
            return
        }

        val nextOpen = !state[OPEN]

        chunk.apply(
            ChunkLocalBlockUpdate.Change(inChunk, state.withProperties(OPEN to nextOpen)),
            ChunkLocalBlockUpdate.Change(otherPosition, otherState.withProperties(OPEN to nextOpen)),
        )
    }

    private fun getShape(hinge: Sides, open: Boolean, facing: Directions): AABB {
        val direction = when {
            !open -> facing.inverted
            hinge == Sides.LEFT -> facing.rotateY(-1)
            hinge == Sides.RIGHT -> facing.rotateY(1)
            else -> Broken()
        }
        return SHAPES[direction.ordinal - Directions.SIDE_OFFSET]
    }

    private fun getLegacyShape(session: PlaySession, position: BlockPosition, state: BlockState): Shape? {
        val isTop = isTop(state, session)
        val other = session.world[position + if (isTop) Directions.DOWN else Directions.UP]
        if (other !is PropertyBlockState || other.block !is DoorBlock) return null
        if (isTop(other, session) == isTop) return null  // impossible


        val top = if (isTop) state else other
        val bottom = if (isTop) other else state


        val hinge = top[HINGE]
        val facing = bottom[FACING]
        val open = bottom[OPEN]

        return getShape(hinge, open, facing)
    }

    override fun getOutlineShape(session: PlaySession, position: BlockPosition, state: BlockState): Shape? {
        if (session.version.flattened) return super.getOutlineShape(session, position, state)

        return getLegacyShape(session, position, state)
    }

    override fun getCollisionShape(session: PlaySession, context: CollisionContext, position: BlockPosition, state: BlockState, blockEntity: BlockEntity?): Shape? {
        if (session.version.flattened) return super.getCollisionShape(session, context, position, state, blockEntity)
        return getLegacyShape(session, position, state)
    }


    override fun bakeModel(context: RenderContext, model: DirectBlockModel) {
        if (context.session.version.flattened) return super.bakeModel(context, model)

        val models: MutableMap<Map<BlockProperty<*>, Any>, BlockRender?> = hashMapOf()
        for (properties in this.properties.unpack()) {
            val renderer = model.choose(properties)?.bake() ?: continue
            models[properties] = renderer
        }
        if (models.isEmpty()) return

        this.model = DoorModel(models)
    }

    companion object {
        val HALF = EnumProperty("half", Halves, Halves.set(Halves.UPPER, Halves.LOWER))
        val HINGE = EnumProperty("hinge", Sides)
        val POWERED = BlockProperties.POWERED
        val FACING = BlockProperties.FACING_HORIZONTAL
        val OPEN = BooleanProperty("open")

        private val SHAPES = arrayOf(
            AABB(0.0, 0.0, 0.0, 1.0, 1.0, 0.1875),
            AABB(0.0, 0.0, 0.8125, 1.0, 1.0, 1.0),
            AABB(0.0, 0.0, 0.0, 0.1875, 1.0, 1.0),
            AABB(0.8125, 0.0, 0.0, 1.0, 1.0, 1.0),
        )
    }

    private class DoorModel(
        val models: Map<Map<BlockProperty<*>, Any>, BlockRender?>
    ) : PickedBlockRender {
        override val default: BlockRender?
            get() = null

        fun pick(half: Halves, hinge: Sides, powered: Boolean, facing: Directions, open: Boolean): BlockRender? {
            return models[mapOf(HALF to half, HINGE to hinge, POWERED to powered, FACING to facing, OPEN to open)]
        }

        override fun pick(state: BlockState, neighbours: Array<BlockState?>): BlockRender? {
            if (state !is PropertyBlockState) return null
            val half = state[HALF]

            val other = if (half == Halves.UPPER) neighbours[Directions.O_DOWN] else neighbours[Directions.O_UP]
            if (other !is PropertyBlockState || other.block !is DoorBlock) return null
            if (other[HALF] == half) return null // double door is invalid

            val top = if (half == Halves.UPPER) state else other
            val bottom = if (half == Halves.UPPER) other else state

            val hinge = top[HINGE]
            val powered = top[POWERED]
            val facing = bottom[FACING]
            val open = bottom[OPEN]

            return pick(half, hinge, powered, facing, open)
        }

    }

    abstract class WoodenDoor(identifier: ResourceLocation, settings: BlockSettings) : DoorBlock(identifier, settings), AxeRequirement {
        override val hardness get() = 3.0f

        override fun interact(session: PlaySession, target: BlockTarget, hand: Hands, stack: ItemStack?): InteractionResults {
            cycleOpen(session, target.blockPosition, target.state)
            return InteractionResults.SUCCESS
        }
    }

    open class IronDoor(identifier: ResourceLocation = Companion.identifier, settings: BlockSettings) : DoorBlock(identifier, settings), PickaxeRequirement {
        override val hardness get() = 5.0f

        override fun interact(session: PlaySession, target: BlockTarget, hand: Hands, stack: ItemStack?): InteractionResults {
            return InteractionResults.FAILED
        }

        companion object : BlockFactory<IronDoor> {
            override val identifier = minecraft("iron_door")

            override fun build(registries: Registries, settings: BlockSettings) = IronDoor(settings = settings)
        }
    }
}
