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

package de.bixilon.minosoft.gui.rendering.light.ao

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.light.ao.AmbientOcclusionUtil.applyBottom
import de.bixilon.minosoft.gui.rendering.light.ao.AmbientOcclusionUtil.applyEast
import de.bixilon.minosoft.gui.rendering.light.ao.AmbientOcclusionUtil.applyNorth
import de.bixilon.minosoft.gui.rendering.light.ao.AmbientOcclusionUtil.applySouth
import de.bixilon.minosoft.gui.rendering.light.ao.AmbientOcclusionUtil.applyTop
import de.bixilon.minosoft.gui.rendering.light.ao.AmbientOcclusionUtil.applyWest

class AmbientOcclusion(
    val section: ChunkSection,
) {
    private var input = Array(Directions.SIZE) { IntArray(AmbientOcclusionUtil.LEVELS) }
    private var output = Array(Directions.SIZE) { AmbientOcclusionUtil.EMPTY }
    private var dirty = 0x00


    fun apply(direction: Directions, position: BlockPosition) = apply(direction, position.x, position.y, position.z)

    fun apply(direction: Directions, x: Int, y: Int, z: Int): IntArray {
        val mask = 1 shl direction.ordinal
        if (dirty and mask != 0) return output[direction.ordinal] // already calculated

        val input = input[direction.ordinal]

        dirty = dirty or mask
        val output = when (direction) {
            Directions.DOWN -> applyBottom(section, x, y, z, input)
            Directions.UP -> applyTop(section, x, y, z, input)

            Directions.NORTH -> applyNorth(section, x, y, z, input)
            Directions.SOUTH -> applySouth(section, x, y, z, input)

            Directions.WEST -> applyWest(section, x, y, z, input)
            Directions.EAST -> applyEast(section, x, y, z, input)
        }
        this.output[direction.ordinal] = output

        return output
    }

    fun clear() {
        dirty = 0x00
    }
}
