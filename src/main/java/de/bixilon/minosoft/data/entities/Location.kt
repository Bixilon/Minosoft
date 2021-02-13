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
package de.bixilon.minosoft.data.entities

import de.bixilon.minosoft.data.world.BlockPosition
import glm_.vec3.Vec3


data class Location(val x: Double, val y: Double, val z: Double) {

    constructor(position: Vec3) : this(position.x.toDouble(), position.y.toDouble(), position.z.toDouble())

    override fun toString(): String {
        return "($x $y $z)"
    }

    fun toVec3(): Vec3 {
        return Vec3(x, y, z)
    }

    companion object {
        @JvmStatic
        fun fromPosition(position: BlockPosition): Location {
            return Location(position.x.toDouble(), position.y.toDouble(), position.z.toDouble())
        }
    }
}
