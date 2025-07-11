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

package de.bixilon.minosoft.gui.rendering.util.vec.vec2

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateSine
import de.bixilon.kutil.primitive.FloatUtil.toFloat

object Vec2Util {
    val EMPTY = Vec2f.EMPTY
    val ONE = Vec2f(1.0f)

    val Vec2f.Companion.MIN: Vec2f
        get() = Vec2f(Float.MIN_VALUE, Float.MIN_VALUE)

    val Vec2f.Companion.EMPTY: Vec2f
        get() = Vec2f(0.0f, 0.0f)

    val Vec2f.Companion.MAX: Vec2f
        get() = Vec2f(Float.MAX_VALUE, Float.MAX_VALUE)


    fun Any?.toVec2f(default: Vec2f? = null): Vec2f {
        return toVec2N() ?: default ?: throw IllegalArgumentException("Not a Vec2: $this")
    }

    fun Any?.toVec2N(): Vec2f? {
        return when (this) {
            is List<*> -> Vec2f(this[0].toFloat(), this[1].toFloat())
            is Map<*, *> -> Vec2f(this["x"]?.toFloat() ?: 0.0f, this["y"]?.toFloat() ?: 0.0f)
            is Number -> Vec2f(this.toFloat())
            else -> null
        }
    }


    fun interpolateLinear(delta: Float, start: Vec2f, end: Vec2f): Vec2f {
        if (delta <= 0.0f) {
            return start
        }
        if (delta >= 1.0f) {
            return end
        }
        return Vec2f(interpolateLinear(delta, start.x, end.x), interpolateLinear(delta, start.y, end.y))
    }

    fun interpolateSine(delta: Float, start: Vec2f, end: Vec2f): Vec2f {
        if (delta <= 0.0f) {
            return start
        }
        if (delta >= 1.0f) {
            return end
        }
        return Vec2f(interpolateSine(delta, start.x, end.x), interpolateSine(delta, start.y, end.y))
    }

    infix fun Vec2f.isSmaller(other: Vec2f): Boolean {
        return this.x < other.x || this.y < other.y
    }

    infix fun Vec2f.isSmallerEquals(other: Vec2f): Boolean {
        return this.x <= other.x || this.y <= other.y
    }

    infix fun Vec2f.isGreater(other: Vec2f): Boolean {
        return this.x > other.x || this.y > other.y
    }

    infix fun Vec2f.isGreaterEquals(other: Vec2f): Boolean {
        return this.x >= other.x || this.y >= other.y
    }

    fun Vec2f.absAssign(): Vec2f {
        if (x < 0) {
            x = -x
        }
        if (y < 0) {
            y = -y
        }
        return this
    }

    val Vec2f.abs: Vec2f
        get() = Vec2f(x, y).absAssign()
}
