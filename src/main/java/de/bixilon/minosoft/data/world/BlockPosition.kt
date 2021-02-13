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

data class BlockPosition(val x: Int, val y: Int, val z: Int) {
    constructor(chunkLocation: ChunkLocation, sectionHeight: Int, inChunkSectionLocation: InChunkSectionLocation) : this(chunkLocation.x * ProtocolDefinition.SECTION_WIDTH_X + inChunkSectionLocation.x, sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y + inChunkSectionLocation.y, chunkLocation.z * ProtocolDefinition.SECTION_WIDTH_Z + inChunkSectionLocation.z) // ToDo

    fun getChunkLocation(): ChunkLocation {
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
        return ChunkLocation(chunkX, chunkY)
    }

    fun getInChunkLocation(): InChunkLocation {
        var x: Int = this.x % ProtocolDefinition.SECTION_WIDTH_X
        if (x < 0) {
            x += ProtocolDefinition.SECTION_WIDTH_X
        }
        var z: Int = this.z % ProtocolDefinition.SECTION_WIDTH_Z
        if (z < 0) {
            z += ProtocolDefinition.SECTION_WIDTH_Z
        }
        return InChunkLocation(x, this.y, z)
    }

    fun getInChunkSectionLocation(): InChunkSectionLocation {
        val location = getInChunkLocation()
        return InChunkSectionLocation(location.x, this.y % 16, location.z)
    }

    fun getSectionHeight(): Int {
        return y / ProtocolDefinition.SECTION_HEIGHT_Y
    }

    override fun toString(): String {
        return "($x $y $z)"
    }
}
