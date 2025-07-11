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

package de.bixilon.minosoft.data.world.positions

import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.minosoft.config.DebugOptions

object BlockPositionUtil {

    fun generatePositionHash(x: Int, y: Int, z: Int): Long {
        var hash = (x * 3129871L) xor (z * 116129781L) xor y.toLong()
        hash = hash * hash * 42317861L + hash * 11L
        return hash shr 16
    }

    inline fun assertPosition(condition: Boolean) {
        if (!DebugOptions.VERIFY_COORDINATES) return
        if (!condition) throw AssertionError("Position assert failed!")
    }

    inline fun assertPosition(value: Int, min: Int, max: Int) {
        if (!DebugOptions.VERIFY_COORDINATES) return
        if (value < min) throw AssertionError("coordinate out of range: $value < $min")
        if (value > max) throw AssertionError("coordinate out of range: $value > $max")
    }


    val BlockPosition.center: Vec3d
        get() = Vec3d(x + 0.5, y + 0.5, z + 0.5)

    val BlockPosition.entityPosition: Vec3d
        get() = Vec3d(x + 0.5, y + 0.0, z + 0.5) // ToDo: Confirm
}
