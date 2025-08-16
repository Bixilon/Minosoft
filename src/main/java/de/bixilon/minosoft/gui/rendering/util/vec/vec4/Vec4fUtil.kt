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

package de.bixilon.minosoft.gui.rendering.util.vec.vec4

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.world.vec.vec4.f.Vec4f
import de.bixilon.minosoft.data.world.vec.vec4.f._Vec4f

object Vec4fUtil {

    inline val _Vec4f.top: Float
        get() = this.x

    inline val _Vec4f.right: Float
        get() = this.y

    inline val _Vec4f.bottom: Float
        get() = this.z

    inline val _Vec4f.left: Float
        get() = this.w

    inline val _Vec4f.horizontal: Float
        get() = right + left

    inline val _Vec4f.vertical: Float
        get() = top + bottom

    inline val _Vec4f.spaceSize: Vec2f
        get() = Vec2f(horizontal, vertical)

    inline val _Vec4f.offset: Vec2f
        get() = Vec2f(left, top)

    fun FloatArray.dot(x: Float, y: Float, z: Float) = this[0] * x + this[1] * y + this[2] * z + this[3]

    fun _Vec4f.copy(top: Float = this.top, right: Float = this.right, bottom: Float = this.bottom, left: Float = this.left): Vec4f {
        return Vec4f(top, right, bottom, left)
    }

    fun marginOf(top: Float = 0.0f, right: Float = 0.0f, bottom: Float = 0.0f, left: Float = 0.0f): Vec4f {
        return Vec4f(top, right, bottom, left)
    }
}
