/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.util.vec.vec3

import de.bixilon.minosoft.util.KUtil.toInt
import glm_.vec3.Vec3i

object Vec3iUtil {

    val Vec3i.Companion.MIN: Vec3i
        get() = Vec3i(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE)

    val Vec3i.Companion.EMPTY: Vec3i
        get() = Vec3i(0, 0, 0)

    val Vec3i.Companion.MAX: Vec3i
        get() = Vec3i(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)


    fun Any?.toVec3i(default: Vec3i? = null): Vec3i {
        return when (this) {
            is List<*> -> Vec3i(this[0].toInt(), this[1].toInt(), this[2].toInt())
            is Map<*, *> -> Vec3i(this["x"]?.toInt() ?: 0.0f, this["y"]?.toInt() ?: 0.0f, this["z"]?.toInt() ?: 0.0f)
            is Number -> Vec3i(this.toInt())
            else -> default ?: throw IllegalArgumentException("Not a Vec3i: $this")
        }
    }

    fun Any?.toVec3iN(default: Vec3i? = null): Vec3i? {
        return when (this) {
            is List<*> -> Vec3i(this[0].toInt(), this[1].toInt(), this[2].toInt())
            is Map<*, *> -> Vec3i(this["x"]?.toInt() ?: 0.0f, this["y"]?.toInt() ?: 0.0f, this["z"]?.toInt() ?: 0.0f)
            is Number -> Vec3i(this.toInt())
            else -> default
        }
    }
}
