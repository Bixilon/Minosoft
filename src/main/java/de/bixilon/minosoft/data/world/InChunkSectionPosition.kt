/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.world

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3

/**
 * Chunk X, Y and Z location (max 16x16x16)
 */
data class InChunkSectionPosition(val x: Int, val y: Int, val z: Int) {

    override fun toString(): String {
        return "($x $y $z)"
    }

    fun getInChunkLocation(sectionHeight: Int): InChunkPosition {
        return InChunkPosition(x, y + ProtocolDefinition.SECTION_HEIGHT_Y * sectionHeight, z)
    }

    operator fun plus(vec3: Vec3): InChunkSectionPosition {
        var nextX = x + vec3.x.toInt()
        var nextY = y + vec3.y.toInt()
        var nextZ = z + vec3.z.toInt()

        if (nextX < 0) {
            nextX = ProtocolDefinition.SECTION_MAX_X
        } else if (nextX > ProtocolDefinition.SECTION_MAX_X) {
            nextX = 0
        }

        if (nextY < 0) {
            nextY = ProtocolDefinition.SECTION_MAX_Y
        } else if (nextY > ProtocolDefinition.SECTION_MAX_Y) {
            nextY = 0
        }

        if (nextZ < 0) {
            nextZ = ProtocolDefinition.SECTION_MAX_Z
        } else if (nextZ > ProtocolDefinition.SECTION_MAX_Z) {
            nextZ = 0
        }

        return InChunkSectionPosition(nextX, nextY, nextZ)
    }

    operator fun plus(directions: Directions): InChunkSectionPosition {
        return this + directions.directionVector
    }
}
