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

package de.bixilon.kmath.mat.mat3.f

import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f._Vec3f
import de.bixilon.kmath.vec.vec4.f.MVec4f
import de.bixilon.kmath.vec.vec4.f._Vec4f

object Mat3Operations {

    inline fun plus(a: _Mat3f, b: Number, result: MMat3f): Unit = TODO()
    inline fun plus(a: _Mat3f, b: _Mat3f, result: MMat3f): Unit = TODO()

    inline fun times(a: _Mat3f, b: Number, result: MMat3f): Unit = TODO()
    inline fun times(a: _Mat3f, b: _Mat3f, result: MMat3f): Unit = TODO()

    inline fun times(a: _Mat3f, b: _Vec3f, result: MVec3f): Unit = TODO()
    inline fun times(a: _Mat3f, b: _Vec4f, result: MVec4f): Unit = TODO()

    inline fun translate(a: _Mat3f, x: Float, y: Float, z: Float, result: MMat3f): Unit = TODO()
    inline fun scale(a: _Mat3f, x: Float, y: Float, z: Float, result: MMat3f): Unit = TODO()
}
