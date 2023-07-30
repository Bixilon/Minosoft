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

package de.bixilon.minosoft.gui.rendering.util.vec.vec4

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec4.Vec4

object Vec4Util {

    val Vec4.Companion.MIN: Vec4
        get() = Vec4(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE)

    val Vec4.Companion.EMPTY: Vec4
        get() = Vec4(0.0f, 0.0f, 0.0f, 0.0f)

    val Vec4.Companion.MAX: Vec4
        get() = Vec4(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)


    val Vec4.top: Float
        get() = this.x

    val Vec4.right: Float
        get() = this.y

    val Vec4.bottom: Float
        get() = this.z

    val Vec4.left: Float
        get() = this.w

    val Vec4.horizontal: Float
        get() = right + left

    val Vec4.vertical: Float
        get() = top + bottom

    val Vec4.spaceSize: Vec2
        get() = Vec2(horizontal, vertical)

    val Vec4.offset: Vec2
        get() = Vec2(left, top)

    fun FloatArray.dot(x: Float, y: Float, z: Float) = this[0] * x + this[1] * y + this[2] * z + this[3]

    fun Vec4.copy(top: Float = this.top, right: Float = this.right, bottom: Float = this.bottom, left: Float = this.left): Vec4 {
        return Vec4(top, right, bottom, left)
    }

    fun marginOf(top: Float = 0.0f, right: Float = 0.0f, bottom: Float = 0.0f, left: Float = 0.0f): Vec4 {
        return Vec4(top, right, bottom, left)
    }
}
