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

import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.world.vec.vec2.i.MVec2i
import de.bixilon.minosoft.data.world.vec.vec2.i.Vec2i
import de.bixilon.minosoft.util.KUtil.rad
import de.bixilon.minosoft.util.f
import kotlin.math.abs

object Vec2iUtil {

    fun Vec2i.min(other: Vec2i): Vec2i {
        val min = MVec2i(this)
        if (other.x < min.x) {
            min.x = other.x
        }
        if (other.y < min.y) {
            min.y = other.y
        }
        return min.unsafe
    }

    fun Vec2i.max(other: Vec2i): Vec2i {
        val max = MVec2i(this)
        if (other.x > max.x) {
            max.x = other.x
        }
        if (other.y > max.y) {
            max.y = other.y
        }
        return max.unsafe
    }

    infix fun Vec2i.isSmaller(other: Vec2i): Boolean {
        return this.x < other.x || this.y < other.y
    }

    infix fun Vec2i.isSmallerEquals(other: Vec2i): Boolean {
        return this.x <= other.x || this.y <= other.y
    }

    infix fun Vec2i.isGreater(other: Vec2i): Boolean {
        return this.x > other.x || this.y > other.y
    }

    infix fun Vec2i.isGreaterEquals(other: Vec2i): Boolean {
        return this.x >= other.x || this.y >= other.y
    }

    val Vec2i.rad: Vec2f
        get() = Vec2f(x.f.rad, y.f.rad)

    val Vec2i.abs: Vec2i
        get() = Vec2i(abs(x), abs(y))

    fun Any?.toVec2i(default: Vec2i? = null): Vec2i {
        return toVec2iN() ?: default ?: throw IllegalArgumentException("Not a Vec2i: $this")
    }

    fun Any?.toVec2iN() = when (this) {
        is List<*> -> Vec2i(this[0].toInt(), this[1].toInt())
        is Map<*, *> -> Vec2i(this["x"]?.toInt() ?: 0, this["y"]?.toInt() ?: 0)
        is Number -> Vec2i(this.toInt())
        else -> null
    }

    fun Vec2i.isOutside(min: Vec2i, max: Vec2i): Boolean {
        return this isSmaller min || this isGreater max
    }
}
