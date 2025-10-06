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

import de.bixilon.kmath.number.FloatUtil.plus
import de.bixilon.kmath.number.FloatUtil.times
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec3.f._Vec3f

object Mat3Operations {

    fun plus(a: Mat3f, b: Float, result: MMat3f) {
        result[0, 0] = a[0, 0] + b; result[0, 1] = a[0, 1] + b; result[0, 2] = a[0, 2] + b
        result[1, 0] = a[1, 0] + b; result[1, 1] = a[1, 1] + b; result[1, 2] = a[1, 2] + b
        result[2, 0] = a[2, 0] + b; result[2, 1] = a[2, 1] + b; result[2, 2] = a[2, 2] + b
    }

    fun plus(a: Mat3f, b: Mat3f, result: MMat3f) {
        result[0, 0] = a[0, 0] + b[0, 0]; result[0, 1] = a[0, 1] + b[0, 1]; result[0, 2] = a[0, 2] + b[0, 2]
        result[1, 0] = a[1, 0] + b[1, 0]; result[1, 1] = a[1, 1] + b[1, 1]; result[1, 2] = a[1, 2] + b[1, 2]
        result[2, 0] = a[2, 0] + b[2, 0]; result[2, 1] = a[2, 1] + b[2, 1]; result[2, 2] = a[2, 2] + b[2, 2]
    }

    fun times(a: Mat3f, b: Float, result: MMat3f) {
        result[0, 0] = a[0, 0] * b; result[0, 1] = a[0, 1] * b; result[0, 2] = a[0, 2] * b
        result[1, 0] = a[1, 0] * b; result[1, 1] = a[1, 1] * b; result[1, 2] = a[1, 2] * b
        result[2, 0] = a[2, 0] * b; result[2, 1] = a[2, 1] * b; result[2, 2] = a[2, 2] * b
    }

    fun times(a: Mat3f, b: Mat3f, result: MMat3f) {
        val x0 = a[0, 0] * b[0, 0] + a[1, 0] * b[0, 1] + a[2, 0] * b[0, 2]
        val x1 = a[0, 1] * b[0, 0] + a[1, 1] * b[0, 1] + a[2, 1] * b[0, 2]
        val x2 = a[0, 2] * b[0, 0] + a[1, 2] * b[0, 1] + a[2, 2] * b[0, 2]

        val y0 = a[0, 0] * b[1, 0] + a[1, 0] * b[1, 1] + a[2, 0] * b[1, 2]
        val y1 = a[0, 1] * b[1, 0] + a[1, 1] * b[1, 1] + a[2, 1] * b[1, 2]
        val y2 = a[0, 2] * b[1, 0] + a[1, 2] * b[1, 1] + a[2, 2] * b[1, 2]

        val z0 = a[0, 0] * b[2, 0] + a[1, 0] * b[2, 1] + a[2, 0] * b[2, 2]
        val z1 = a[0, 1] * b[2, 0] + a[1, 1] * b[2, 1] + a[2, 1] * b[2, 2]
        val z2 = a[0, 2] * b[2, 0] + a[1, 2] * b[2, 1] + a[2, 2] * b[2, 2]

        result[0, 0] = x0; result[1, 0] = y0; result[2, 0] = z0
        result[0, 1] = x1; result[1, 1] = y1; result[2, 1] = z1
        result[0, 2] = x2; result[1, 2] = y2; result[2, 2] = z2
    }

    fun times(a: Mat3f, b: Vec3f, result: MVec3f) {
        result.x = a[0, 0] * b.x + a[1, 0] * b.y + a[2, 0] * b.z
        result.y = a[0, 1] * b.x + a[1, 1] * b.y + a[2, 1] * b.z
        result.z = a[0, 2] * b.x + a[1, 2] * b.y + a[2, 2] * b.z
    }
}
