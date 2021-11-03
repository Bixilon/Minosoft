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

package de.bixilon.minosoft.util

import de.bixilon.minosoft.util.KUtil.decide
import glm_.vec2.Vec2i
import kotlin.math.floor

object MMath {

    fun clamp(value: Vec2i, min: Vec2i, max: Vec2i): Vec2i {
        value.x = clamp(value.x, min.x, max.x)
        value.y = clamp(value.y, min.y, max.y)
        return value
    }

    fun clamp(value: Int, min: Int, max: Int): Int {
        if (value < min) {
            return min
        }
        if (value > max) {
            return max
        }
        return value
    }

    fun clamp(value: Float, min: Float, max: Float): Float {
        if (value < min) {
            return min
        }
        if (value > max) {
            return max
        }
        return value
    }

    fun clamp(value: Double, min: Double, max: Double): Double {
        if (value < min) {
            return min
        }
        if (value > max) {
            return max
        }
        return value
    }

    fun divideUp(value: Int, divider: Int): Int {
        return (value + divider - 1) / divider
    }

    fun divideUp(value: Float, divider: Float): Float {
        return (value + divider - 1.0f) / divider
    }

    fun round10(value: Float): Int {
        return ((value * 10).toInt() + 5) / 10
    }

    val Float.round10: Float get() = (this * 10).toInt().toFloat() / 10f

    val Double.round10: Double get() = (this * 10).toInt().toDouble() / 10.0

    fun round10Up(value: Float): Int {
        val intValue = value.toInt()
        val rest = value / intValue
        if (rest > 0) {
            return intValue + 1
        }
        return intValue
    }

    fun square(d: Double): Double {
        return d * d
    }

    fun fractionalPart(value: Double): Double {
        return value - floor(value)
    }

    val Boolean.positiveNegative: Int
        get() = if (this) {
            1
        } else {
            -1
        }

    val Double.floor: Int
        get() {
            val int = this.toInt()
            return (this < int).decide(int - 1, int)
        }

    val Float.floor: Int
        get() {
            val int = this.toInt()
            return (this < int).decide(int - 1, int)
        }

    val Double.ceil: Int
        get() {
            val int = this.toInt()
            return (this > int).decide(int + 1, int)
        }

    val Float.ceil: Int
        get() {
            val int = this.toInt()
            return (this > int).decide(int + 1, int)
        }
}
