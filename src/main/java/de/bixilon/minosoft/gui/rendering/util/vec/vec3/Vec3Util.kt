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

import de.bixilon.minosoft.util.KUtil.toFloat
import glm_.vec3.Vec3

object Vec3Util {

    val Vec3.Companion.MIN: Vec3
        get() = Vec3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE)

    val Vec3.Companion.EMPTY: Vec3
        get() = Vec3(0.0f, 0.0f, 0.0f)

    val Vec3.Companion.MAX: Vec3
        get() = Vec3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)


    fun Any?.toVec3(default: Vec3? = null): Vec3 {
        return when (this) {
            is List<*> -> Vec3(this[0].toFloat(), this[1].toFloat(), this[2].toFloat())
            is Map<*, *> -> Vec3(this["x"]?.toFloat() ?: 0.0f, this["y"]?.toFloat() ?: 0.0f, this["z"]?.toFloat() ?: 0.0f)
            is Number -> Vec3(this.toFloat())
            else -> default ?: throw IllegalArgumentException("Not a Vec3: $this")
        }
    }
}
