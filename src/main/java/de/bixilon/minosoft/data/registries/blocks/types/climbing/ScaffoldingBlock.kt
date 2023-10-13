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

package de.bixilon.minosoft.data.registries.blocks.types.climbing

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactory
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.CollisionContext
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EntityCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.properties.hardness.InstantBreakableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.item.BlockWithItem
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.transparency.TransparentBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.block.climbing.ClimbingItems
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape
import de.bixilon.minosoft.data.registries.shapes.voxel.VoxelShape
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

open class ScaffoldingBlock(identifier: ResourceLocation = ScaffoldingBlock.identifier, settings: BlockSettings) : Block(identifier, settings), ClimbingBlock, TransparentBlock, InstantBreakableBlock, OutlinedBlock, CollidableBlock, BlockWithItem<ClimbingItems.ScaffoldingItem> {
    override val item: ClimbingItems.ScaffoldingItem = this::item.inject(ClimbingItems.ScaffoldingItem)

    override fun canPushOut(entity: Entity) = false


    override fun getOutlineShape(connection: PlayConnection, position: BlockPosition, state: BlockState): AbstractVoxelShape? {
        if (connection.player.items.inventory[EquipmentSlots.MAIN_HAND]?.item?.item is ClimbingItems.ScaffoldingItem) {
            return AbstractVoxelShape.FULL
        }
        return if (state.isBottom()) BOTTOM_OUTLINE else OUTLINE
    }

    override fun getCollisionShape(connection: PlayConnection, context: CollisionContext, position: Vec3i, state: BlockState, blockEntity: BlockEntity?): AbstractVoxelShape? {
        if (context.isAbove(1.0, position) && (context !is EntityCollisionContext || !context.descending)) {
            return OUTLINE
        }
        val distance = state[BlockProperties.DISTANCE]

        if (distance == 0 || !state.isBottom() || !context.isAbove(0.0, position)) {
            return null
        }
        return COLLISION
    }

    companion object : BlockFactory<ScaffoldingBlock> {
        override val identifier = minecraft("scaffolding")
        private val COLLISION = VoxelShape(0.0, 0.0, 0.0, 1.0, 0.125, 1.0)
        private val OUTLINE = VoxelShape(
            AABB(0.0, 0.875, 0.0, 1.0, 1.0, 1.0),      // top
            AABB(0.0, 0.0, 0.0, 0.125, 0.875, 0.125),
            AABB(0.875, 0.0, 0.0, 1.0, 0.875, 0.125),
            AABB(0.0, 0.0, 0.875, 0.125, 0.875, 1.0),
            AABB(0.875, 0.0, 0.875, 1.0, 0.875, 1.0),
        )
        private val BOTTOM_OUTLINE = VoxelShape(
            AABB(0.0, 0.875, 0.0, 1.0, 1.0, 1.0),       // top
            AABB(0.0, 0.0, 0.0, 1.0, 0.125, 1.0),       // bottom
            AABB(0.0, 0.125, 0.0, 0.125, 0.875, 0.125),
            AABB(0.875, 0.125, 0.0, 1.0, 0.875, 0.125),
            AABB(0.0, 0.125, 0.875, 0.125, 0.875, 1.0),
            AABB(0.875, 0.125, 0.875, 1.0, 0.875, 1.0),
        )


        private fun BlockState.isBottom(): Boolean {
            return this[BlockProperties.SCAFFOLDING_BOTTOM]
        }

        override fun build(registries: Registries, settings: BlockSettings) = ScaffoldingBlock(settings = settings)
    }
}
