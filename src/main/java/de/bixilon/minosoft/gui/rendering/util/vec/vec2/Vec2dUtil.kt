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

package de.bixilon.minosoft.gui.rendering.util.vec.vec2

import de.bixilon.minosoft.util.KUtil.toDouble
import glm_.vec2.Vec2d

object Vec2dUtil {

    val Vec2d.Companion.MIN: Vec2d
        get() = Vec2d(Double.MIN_VALUE, Double.MIN_VALUE)

    val Vec2d.Companion.EMPTY: Vec2d
        get() = Vec2d(0.0, 0.0)

    val Vec2d.Companion.MAX: Vec2d
        get() = Vec2d(Double.MAX_VALUE, Double.MAX_VALUE)


    fun Any?.toVec2d(default: Vec2d? = null): Vec2d {
        return toVec2dN() ?: default ?: throw IllegalArgumentException("Not a Vec2d: $this")
    }

    fun Any?.toVec2dN(): Vec2d? {
        return when (this) {
            is List<*> -> Vec2d(this[0].toDouble(), this[1].toDouble())
            is Map<*, *> -> Vec2d(this["x"]?.toDouble() ?: 0.0, this["y"]?.toDouble() ?: 0.0)
            is Number -> Vec2d(this.toDouble())
            else -> null
        }
    }
}
