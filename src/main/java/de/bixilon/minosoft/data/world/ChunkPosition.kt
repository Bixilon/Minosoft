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
import glm_.vec3.Vec3

data class ChunkPosition(val x: Int, val z: Int) {
    override fun toString(): String {
        return "($x $z)"
    }

    operator fun plus(vec3: Vec3): ChunkPosition {
        return ChunkPosition(x + vec3.x.toInt(), z + vec3.z.toInt())
    }

    operator fun plus(direction: Directions): ChunkPosition {
        return this + direction.directionVector
    }
}
