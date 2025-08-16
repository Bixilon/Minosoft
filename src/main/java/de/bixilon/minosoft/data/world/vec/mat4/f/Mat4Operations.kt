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

package de.bixilon.minosoft.data.world.vec.mat4.f

import de.bixilon.minosoft.data.world.vec.vec3.f.MVec3f
import de.bixilon.minosoft.data.world.vec.vec3.f._Vec3f
import de.bixilon.minosoft.data.world.vec.vec4.f.MVec4f
import de.bixilon.minosoft.data.world.vec.vec4.f._Vec4f
import de.bixilon.minosoft.util.f

object Mat4Operations {

    inline fun plus(a: _Mat4f, b: Number, result: MMat4f): Unit = TODO()
    inline fun plus(a: _Mat4f, b: _Mat4f, result: MMat4f): Unit = TODO()

    inline fun times(a: _Mat4f, b: Number, result: MMat4f): Unit = TODO()
    inline fun times(a: _Mat4f, b: _Mat4f, result: MMat4f): Unit = TODO()

    inline fun times(a: _Mat4f, b: _Vec3f, result: MVec3f): Unit = TODO()
    inline fun times(a: _Mat4f, b: _Vec4f, result: MVec4f): Unit = TODO()

    inline fun translate(a: _Mat4f, x: Float, y: Float, z: Float, result: MMat4f): Unit = TODO()
    inline fun scale(a: _Mat4f, x: Float, y: Float, z: Float, result: MMat4f): Unit = TODO()


    inline operator fun times(number: Number) = Mat4f(
        this[0, 0] * number.f, this[0, 1] * number.f, this[0, 2] * number.f, this[0, 3] * number.f,
        this[1, 0] * number.f, this[1, 1] * number.f, this[1, 2] * number.f, this[1, 3] * number.f,
        this[2, 0] * number.f, this[2, 1] * number.f, this[2, 2] * number.f, this[2, 3] * number.f,
        this[3, 0] * number.f, this[3, 1] * number.f, this[3, 2] * number.f, this[3, 3] * number.f,
    )

    inline operator fun plus(number: Number) = Mat4f(
        this[0, 0] + number.f, this[0, 1] + number.f, this[0, 2] + number.f, this[0, 3] + number.f,
        this[1, 0] + number.f, this[1, 1] + number.f, this[1, 2] + number.f, this[1, 3] + number.f,
        this[2, 0] + number.f, this[2, 1] + number.f, this[2, 2] + number.f, this[2, 3] + number.f,
        this[3, 0] + number.f, this[3, 1] + number.f, this[3, 2] + number.f, this[3, 3] + number.f,
    )

    inline operator fun plus(other: _Mat4f) = Mat4f(
        this[0, 0] + other[0, 0], this[0, 1] + other[0, 1], this[0, 2] + other[0, 2], this[0, 3] + other[0, 3],
        this[1, 0] + other[1, 0], this[1, 1] + other[1, 1], this[1, 2] + other[1, 2], this[1, 3] + other[1, 3],
        this[2, 0] + other[2, 0], this[2, 1] + other[2, 1], this[2, 2] + other[2, 2], this[2, 3] + other[2, 3],
        this[3, 0] + other[3, 0], this[3, 1] + other[3, 1], this[3, 2] + other[3, 2], this[3, 3] + other[3, 3],
    )
}
