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

    fun getChunkLocation(): ChunkLocation {
        return ChunkLocation(this.x / ProtocolDefinition.SECTION_WIDTH_X, this.z / ProtocolDefinition.SECTION_WIDTH_Z)
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

    override fun toString(): String {
        return String.format("%d %d %d", x, y, z)
    }
}
