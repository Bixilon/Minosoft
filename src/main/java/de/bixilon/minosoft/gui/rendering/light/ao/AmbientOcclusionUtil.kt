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

import de.bixilon.minosoft.data.direction.Directions.Companion.O_DOWN
import de.bixilon.minosoft.data.direction.Directions.Companion.O_EAST
import de.bixilon.minosoft.data.direction.Directions.Companion.O_NORTH
import de.bixilon.minosoft.data.direction.Directions.Companion.O_SOUTH
import de.bixilon.minosoft.data.direction.Directions.Companion.O_UP
import de.bixilon.minosoft.data.direction.Directions.Companion.O_WEST
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

object AmbientOcclusionUtil {
    const val LEVELS = 4
    const val LEVEL_NONE = 0
    const val LEVEL_1 = 1
    const val LEVEL_2 = 2
    const val LEVEL_3 = 3

    val EMPTY = IntArray(LEVELS) { LEVEL_NONE }

    fun ChunkSection?.trace(x: Int, y: Int, z: Int): Int {
        if (this == null) return 0
        var x = x
        var y = y
        var z = z

        var section: ChunkSection? = this

        if (x < -1 || x > 16 || y < -1 || y > 16 || z < -1 || z > 16) throw IllegalArgumentException("x=$x, y=$y, z=$z")

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


    fun setY(section: ChunkSection?, position: InSectionPosition, flip: Boolean, ao: IntArray): IntArray {
        if (section == null || section.blocks.isEmpty) return EMPTY

        val west = section.trace(position.x - 1, position.y, position.z + 0)
        val north = section.trace(position.x + 0, position.y, position.z - 1)
        val east = section.trace(position.x + 1, position.y, position.z + 0)
        val south = section.trace(position.x + 0, position.y, position.z + 1)

        ao[0] = calculateLevel(west, north, section.trace(position.x - 1, position.y, position.z - 1))
        ao[2] = calculateLevel(east, south, section.trace(position.x + 1, position.y, position.z + 1))
        ao[if (flip) 3 else 1] = calculateLevel(north, east, section.trace(position.x + 1, position.y, position.z - 1))
        ao[if (flip) 1 else 3] = calculateLevel(south, west, section.trace(position.x - 1, position.y, position.z + 1))

        return ao
    }

    fun applyBottom(section: ChunkSection, position: InSectionPosition, ao: IntArray): IntArray {
        if (position.y == 0) {
            return setY(section.neighbours?.get(O_DOWN), position.with(y = ProtocolDefinition.SECTION_MAX_Y), true, ao)
        } else {
            return setY(section, position.minusY(), true, ao)
        }
    }

    fun applyTop(section: ChunkSection, position: InSectionPosition, ao: IntArray): IntArray {
        if (position.y == ProtocolDefinition.SECTION_MAX_Y) {
            return setY(section.neighbours?.get(O_UP), position.with(y = 0), false, ao)
        } else {
            return setY(section, position.plusY(), false, ao)
        }
    }

    fun setZ(section: ChunkSection?, position: InSectionPosition, flip: Boolean, ao: IntArray): IntArray {
        if (section == null || section.blocks.isEmpty) return EMPTY

        val down = section.trace(position.x + 0, position.y - 1, position.z)
        val west = section.trace(position.x - 1, position.y + 0, position.z)
        val up = section.trace(position.x + 0, position.y + 1, position.z)
        val east = section.trace(position.x + 1, position.y + 0, position.z)

        ao[0] = calculateLevel(down, west, section.trace(position.x - 1, position.y - 1, position.z))
        ao[if (flip) 3 else 1] = calculateLevel(west, up, section.trace(position.x - 1, position.y + 1, position.z))
        ao[2] = calculateLevel(up, east, section.trace(position.x + 1, position.y + 1, position.z))
        ao[if (flip) 1 else 3] = calculateLevel(east, down, section.trace(position.x + 1, position.y - 1, position.z))

        return ao
    }


    fun applyNorth(section: ChunkSection, position: InSectionPosition, ao: IntArray): IntArray {
        if (position.z == 0) {
            return setZ(section.neighbours?.get(O_NORTH), position.with(z = ProtocolDefinition.SECTION_MAX_Z), true, ao)
        } else {
            return setZ(section, position.minusZ(), true, ao)
        }
    }

    fun applySouth(section: ChunkSection, position: InSectionPosition, ao: IntArray): IntArray {
        if (position.z == ProtocolDefinition.SECTION_MAX_Z) {
            return setZ(section.neighbours?.get(O_SOUTH), position.with(z = 0), false, ao)
        } else {
            return setZ(section, position.plusZ(), false, ao)
        }
    }

    fun setX(section: ChunkSection?, position: InSectionPosition, flip: Boolean, ao: IntArray): IntArray {
        if (section == null || section.blocks.isEmpty) return EMPTY

        val down = section.trace(position.x + 0, position.y - 1, position.z)
        val north = section.trace(position.x, position.y + 0, position.z - 1)
        val up = section.trace(position.x, position.y + 1, position.z)
        val south = section.trace(position.x, position.y + 0, position.z + 1)

        ao[0] = calculateLevel(down, north, section.trace(position.x, position.y - 1, position.z - 1))
        ao[if (flip) 3 else 1] = calculateLevel(north, up, section.trace(position.x, position.y + 1, position.z - 1))
        ao[2] = calculateLevel(up, south, section.trace(position.x, position.y + 1, position.z + 1))
        ao[if (flip) 1 else 3] = calculateLevel(south, down, section.trace(position.x, position.y - 1, position.z + 1))

        return ao
    }

    fun applyWest(section: ChunkSection, position: InSectionPosition, ao: IntArray): IntArray {
        if (position.z == 0) {
            return setX(section.neighbours?.get(O_WEST), position.with(x = ProtocolDefinition.SECTION_MAX_X), true, ao)
        } else {
            return setX(section, position.minusX(), false, ao)
        }
    }

    fun applyEast(section: ChunkSection, position: InSectionPosition, ao: IntArray): IntArray {
        if (position.z == ProtocolDefinition.SECTION_MAX_X) {
            return setX(section.neighbours?.get(O_EAST), position.with(x = 0), true, ao)
        } else {
            return setX(section, position.plusX(), true, ao)
        }
    }
}
