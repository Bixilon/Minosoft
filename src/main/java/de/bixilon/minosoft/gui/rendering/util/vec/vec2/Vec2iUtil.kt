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

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.util.KUtil.toInt
import glm_.func.rad
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

object Vec2iUtil {

    val Vec2i.Companion.MIN: Vec2i
        get() = Vec2i(Int.MIN_VALUE, Int.MIN_VALUE)

    val Vec2i.Companion.EMPTY: Vec2i
        get() = Vec2i(0, 0)

    val Vec2i.Companion.MAX: Vec2i
        get() = Vec2i(Int.MAX_VALUE, Int.MAX_VALUE)

    fun Vec2i.min(other: Vec2i): Vec2i {
        val min = Vec2i(this)
        if (other.x < min.x) {
            min.x = other.x
        }
        if (other.y < min.y) {
            min.y = other.y
        }
        return min
    }

    fun Vec2i.max(other: Vec2i): Vec2i {
        val max = Vec2i(this)
        if (other.x > max.x) {
            max.x = other.x
        }
        if (other.y > max.y) {
            max.y = other.y
        }
        return max
    }

    infix fun Vec2i.isSmaller(other: Vec2i): Boolean {
        return this.x < other.x || this.y < other.y
    }

    infix fun Vec2i.isGreater(other: Vec2i): Boolean {
        return this.x > other.x || this.y > other.y
    }

    val Vec2i.rad: Vec2
        get() = Vec2(x.rad, y.rad)

    operator fun Vec2i.get(axis: Axes): Int {
        return when (axis) {
            Axes.X -> x
            Axes.Y -> y
            Axes.Z -> throw IllegalArgumentException("A Vec2i has no Z coordinate!")
        }
    }

    fun Any?.toVec2i(default: Vec2i? = null): Vec2i {
        return toVec2iN() ?: default ?: throw IllegalArgumentException("Not a Vec2i: $this")
    }

    fun Any?.toVec2iN(): Vec2i? {
        return when (this) {
            is List<*> -> Vec2i(this[0].toInt(), this[1].toInt())
            is Map<*, *> -> Vec2i(this["x"]?.toInt() ?: 0, this["y"]?.toInt() ?: 0)
            is Number -> Vec2i(this.toInt())
            else -> null
        }
    }
}
