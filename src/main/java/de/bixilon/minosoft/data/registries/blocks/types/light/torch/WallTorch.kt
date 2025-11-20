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

package de.bixilon.minosoft.data.registries.blocks.types.light.torch

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.shape.Shape

interface WallTorch : OutlinedBlock {

    override fun getOutlineShape(state: BlockState): AABB? {
        return SHAPES[state[FACING].ordinal - Directions.SIDE_OFFSET]
    }

    companion object {
        val FACING = BlockProperties.FACING_HORIZONTAL


        val SHAPES = arrayOf(
            AABB(0.34375, 0.1875, 0.6875, 0.65625, 0.8125, 1.0),
            AABB(0.34375, 0.1875, 0.0, 0.65625, 0.8125, 0.3125),
            AABB(0.6875, 0.1875, 0.34375, 1.0, 0.8125, 0.65625),
            AABB(0.0, 0.1875, 0.34375, 0.3125, 0.8125, 0.65625),
        )
    }
}
