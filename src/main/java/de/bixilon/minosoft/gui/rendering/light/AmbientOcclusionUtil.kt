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

package de.bixilon.minosoft.gui.rendering.light

import de.bixilon.minosoft.data.direction.Directions.Companion.O_DOWN
import de.bixilon.minosoft.data.direction.Directions.Companion.O_EAST
import de.bixilon.minosoft.data.direction.Directions.Companion.O_NORTH
import de.bixilon.minosoft.data.direction.Directions.Companion.O_SOUTH
import de.bixilon.minosoft.data.direction.Directions.Companion.O_UP
import de.bixilon.minosoft.data.direction.Directions.Companion.O_WEST
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

object AmbientOcclusionUtil {
    const val LEVEL_NONE = 0
    const val LEVEL_1 = 1
    const val LEVEL_2 = 2
    const val LEVEL_3 = 3

    fun ChunkSection?.trace(x: Int, y: Int, z: Int): Int {
        if (this == null) return 0
        var x = x
        var y = y
        var z = z

        var section: ChunkSection? = this

        if (x < 0) {
            section = section?.neighbours?.get(O_WEST)
            x = ProtocolDefinition.SECTION_MAX_X
        } else if (x > ProtocolDefinition.SECTION_MAX_X) {
            section = section?.neighbours?.get(O_EAST)
            x = 0
        }

        if (y < 0) {
            section = section?.neighbours?.get(O_DOWN)
            y = ProtocolDefinition.SECTION_MAX_Y
        } else if (y > ProtocolDefinition.SECTION_MAX_Y) {
            section = section?.neighbours?.get(O_UP)
            y = 0
        }
        if (z < 0) {
            section = section?.neighbours?.get(O_NORTH)
            z = ProtocolDefinition.SECTION_MAX_Z
        } else if (z > ProtocolDefinition.SECTION_MAX_Z) {
            section = section?.neighbours?.get(O_SOUTH)
            z = 0
        }

        val state = section?.blocks?.get(x, y, z) ?: return 0
        if (BlockStateFlags.FULLY_OPAQUE in state.flags) return 1

        // TODO: more test?

        return 0
    }


    fun calculateLevel(side1: Int, side2: Int, corner: Int): Int {
        if (side1 and side2 == 0x01) return LEVEL_3
        return side1 + side2 + corner
    }

    private fun IntArray.clear() {
        this[0] = LEVEL_NONE
        this[1] = LEVEL_NONE
        this[2] = LEVEL_NONE
        this[3] = LEVEL_NONE
    }

    fun setY(section: ChunkSection?, x: Int, y: Int, z: Int, flip: Boolean, ao: IntArray) {
        ao.clear()
        if (section == null || section.blocks.isEmpty) return

        val west = section.trace(x - 1, y, z + 0)
        val north = section.trace(x + 0, y, z - 1)
        val east = section.trace(x + 1, y, z + 0)
        val south = section.trace(x + 0, y, z + 1)

        ao[0] = calculateLevel(west, north, section.trace(x - 1, y, z - 1))
        ao[2] = calculateLevel(east, south, section.trace(x + 1, y, z + 1))
        ao[if (flip) 3 else 1] = calculateLevel(north, east, section.trace(x + 1, y, z - 1))
        ao[if (flip) 1 else 3] = calculateLevel(south, west, section.trace(x - 1, y, z + 1))
    }

    fun setTop(section: ChunkSection, x: Int, y: Int, z: Int, ao: IntArray) {
        var section: ChunkSection? = section
        var y = y + 1
        if (y > ProtocolDefinition.SECTION_MAX_Y) {
            section = section?.neighbours?.get(O_UP)
            y = 0
        }

        setY(section, x, y, z, false, ao)
    }

    fun setBottom(section: ChunkSection, x: Int, y: Int, z: Int, ao: IntArray) {
        var section: ChunkSection? = section
        var y = y - 1
        if (y < 0) {
            section = section?.neighbours?.get(O_DOWN)
            y = ProtocolDefinition.SECTION_MAX_Y
        }

        setY(section, x, y, z, true, ao)
    }
}
