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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec4.Vec4i

object Vec4iUtil {

    val Vec4i.Companion.MIN: Vec4i
        get() = Vec4i(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE)

    val Vec4i.Companion.EMPTY: Vec4i
        get() = Vec4i(0, 0, 0, 0)

    val Vec4i.Companion.MAX: Vec4i
        get() = Vec4i(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)


    val Vec4i.top: Int
        get() = this.x

    val Vec4i.right: Int
        get() = this.y

    val Vec4i.bottom: Int
        get() = this.z

    val Vec4i.left: Int
        get() = this.w

    val Vec4i.horizontal: Int
        get() = right + left

    val Vec4i.vertical: Int
        get() = top + bottom

    val Vec4i.spaceSize: Vec2i
        get() = Vec2i(horizontal, vertical)

    val Vec4i.offset: Vec2i
        get() = Vec2i(left, top)


    fun Vec4i.copy(top: Int = this.top, right: Int = this.right, bottom: Int = this.bottom, left: Int = this.left): Vec4i {
        return Vec4i(top, right, bottom, left)
    }

    fun marginOf(top: Int = 0, right: Int = 0, bottom: Int = 0, left: Int = 0): Vec4i {
        return Vec4i(top, right, bottom, left)
    }
}
