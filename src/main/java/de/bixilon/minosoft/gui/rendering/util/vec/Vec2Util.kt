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

package de.bixilon.minosoft.gui.rendering.util.vec

import glm_.vec2.Vec2
import glm_.vec2.Vec2i

object Vec2Util {

    val Vec2.Companion.MIN: Vec2
        get() = Vec2(Float.MIN_VALUE, Float.MIN_VALUE)

    val Vec2.Companion.EMPTY: Vec2
        get() = Vec2(0.0f, 0.0f)

    val Vec2.Companion.MAX: Vec2
        get() = Vec2(Float.MAX_VALUE, Float.MAX_VALUE)


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
}
