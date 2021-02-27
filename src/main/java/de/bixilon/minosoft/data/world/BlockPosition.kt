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

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3

data class BlockPosition(val x: Int, val y: Int, val z: Int) {
    constructor(chunkPosition: ChunkPosition, sectionHeight: Int, inChunkSectionPosition: InChunkSectionPosition) : this(chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X + inChunkSectionPosition.x, sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y + inChunkSectionPosition.y, chunkPosition.z * ProtocolDefinition.SECTION_WIDTH_Z + inChunkSectionPosition.z) // ToDo

    fun getChunkLocation(): ChunkPosition {
        val chunkX = if (this.x >= 0) {
            this.x / ProtocolDefinition.SECTION_WIDTH_X
        } else {
            ((this.x + 1) / ProtocolDefinition.SECTION_WIDTH_X) - 1
        }
        val chunkY = if (this.z >= 0) {
            this.z / ProtocolDefinition.SECTION_WIDTH_Z
        } else {
            ((this.z + 1) / ProtocolDefinition.SECTION_WIDTH_Z) - 1
        }
        return ChunkPosition(chunkX, chunkY)
    }

    fun getInChunkLocation(): InChunkPosition {
        var x: Int = this.x % ProtocolDefinition.SECTION_WIDTH_X
        if (x < 0) {
            x += ProtocolDefinition.SECTION_WIDTH_X
        }
        var z: Int = this.z % ProtocolDefinition.SECTION_WIDTH_Z
        if (z < 0) {
            z += ProtocolDefinition.SECTION_WIDTH_Z
        }
        return InChunkPosition(x, this.y, z)
    }

    fun getInChunkSectionLocation(): InChunkSectionPosition {
        val location = getInChunkLocation()
        return InChunkSectionPosition(location.x, getSectionHeight(), location.z)
    }

    fun getSectionHeight(): Int {
        return if (y < 0) {
            (y + 1) / ProtocolDefinition.SECTION_HEIGHT_Y - 1
        } else {
            y / ProtocolDefinition.SECTION_HEIGHT_Y
        }
    }

    fun toVec3(): Vec3 {
        return Vec3(x, y, z)
    }

    override fun toString(): String {
        return "($x $y $z)"
    }
}
