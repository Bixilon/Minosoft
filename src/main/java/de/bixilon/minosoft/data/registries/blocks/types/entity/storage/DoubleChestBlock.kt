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

import de.bixilon.minosoft.data.direction.DirectionUtil.rotateY
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.container.storage.StorageBlockEntity
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.ChestTypes
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.data.registries.shapes.voxel.VoxelShape
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

interface DoubleChestBlock<T : StorageBlockEntity> : ChestBlock<T> {

    override fun getOutlineShape(connection: PlayConnection, position: BlockPosition, state: BlockState): VoxelShape {
        if (state !is PropertyBlockState) return super.getOutlineShape(connection, position, state)
        val type = state.properties[BlockProperties.CHEST_TYPE] ?: return ChestBlock.SINGLE
        if (type == ChestTypes.SINGLE) return ChestBlock.SINGLE
        var facing = state[BlockProperties.FACING] // TODO: HORIZONTAL_FACING

        facing = facing.rotateY(if (type == ChestTypes.LEFT) 1 else -1)
        // TODO: legacy: check if neighbour block is chest

        return SHAPES[facing.ordinal - Directions.SIDE_OFFSET]
    }

    companion object {
        val SHAPES = arrayOf(
            VoxelShape(0.0625, 0.0, 0.0, 0.9375, 0.875, 0.9375),
            VoxelShape(0.0625, 0.0, 0.0625, 0.9375, 0.875, 1.0),
            VoxelShape(0.0, 0.0, 0.0625, 0.9375, 0.875, 0.9375),
            VoxelShape(0.0625, 0.0, 0.0625, 1.0, 0.875, 0.9375),
        )
    }
}
