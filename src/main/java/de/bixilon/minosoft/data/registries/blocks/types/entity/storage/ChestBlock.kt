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

package de.bixilon.minosoft.data.registries.blocks.types.entity.storage

import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.entities.block.container.storage.StorageBlockEntity
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.CollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.shapes.voxel.VoxelShape
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

interface ChestBlock<T : StorageBlockEntity> : StorageBlock<T>, OutlinedBlock, CollidableBlock {

    override fun getOutlineShape(session: PlaySession, position: BlockPosition, state: BlockState) = SINGLE
    override fun getCollisionShape(session: PlaySession, context: CollisionContext, position: BlockPosition, state: BlockState, blockEntity: BlockEntity?) = getOutlineShape(session, position, state)

    companion object {
        val SINGLE = VoxelShape(0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375)
    }
}
