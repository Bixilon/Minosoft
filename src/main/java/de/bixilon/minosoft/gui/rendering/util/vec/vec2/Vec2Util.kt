/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateSine
import de.bixilon.kutil.primitive.FloatUtil.toFloat

object Vec2Util {

    val Vec2.Companion.MIN: Vec2
        get() = Vec2(Float.MIN_VALUE, Float.MIN_VALUE)

    val Vec2.Companion.EMPTY: Vec2
        get() = Vec2(0.0f, 0.0f)

    val Vec2.Companion.MAX: Vec2
        get() = Vec2(Float.MAX_VALUE, Float.MAX_VALUE)


    fun Any?.toVec2(default: Vec2? = null): Vec2 {
        return toVec2N() ?: default ?: throw IllegalArgumentException("Not a Vec2: $this")
    }

    fun Any?.toVec2N(): Vec2? {
        return when (this) {
            is List<*> -> Vec2(this[0].toFloat(), this[1].toFloat())
            is Map<*, *> -> Vec2(this["x"]?.toFloat() ?: 0.0f, this["y"]?.toFloat() ?: 0.0f)
            is Number -> Vec2(this.toFloat())
            else -> null
        }
    }


    fun interpolateLinear(delta: Float, start: Vec2, end: Vec2): Vec2 {
        if (delta <= 0.0f) {
            return start
        }
        if (delta >= 1.0f) {
            return end
        }
        return Vec2(interpolateLinear(delta, start.x, end.x), interpolateLinear(delta, start.y, end.y))
    }

    fun interpolateSine(delta: Float, start: Vec2, end: Vec2): Vec2 {
        if (delta <= 0.0f) {
            return start
        }
        if (delta >= 1.0f) {
            return end
        }
        return Vec2(interpolateSine(delta, start.x, end.x), interpolateSine(delta, start.y, end.y))
    }

    infix fun Vec2.isSmaller(other: Vec2): Boolean {
        return this.x < other.x || this.y < other.y
    }

    infix fun Vec2.isSmallerEquals(other: Vec2): Boolean {
        return this.x <= other.x || this.y <= other.y
    }

    infix fun Vec2.isGreater(other: Vec2): Boolean {
        return this.x > other.x || this.y > other.y
    }

    infix fun Vec2.isGreaterEquals(other: Vec2): Boolean {
        return this.x >= other.x || this.y >= other.y
    }

    fun Vec2.absAssign(): Vec2 {
        if (x < 0) {
            x = -x
        }
        if (y < 0) {
            y = -y
        }
        return this
    }

    val Vec2.abs: Vec2
        get() = Vec2(x, y).absAssign()
}
