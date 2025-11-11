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

package de.bixilon.minosoft.gui.rendering.camera.frustum

import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import kotlin.math.sqrt

object FrustumUtil {
    const val PLANES = 6

    fun calculatePlanes(matrix: Mat4f): Array<Vec4f> {
        val planes = arrayOf(
            matrix[3] + matrix[0], // left
            matrix[3] - matrix[0], // right

            matrix[3] + matrix[1], // bottom
            matrix[3] - matrix[1], // top

            matrix[3] + matrix[2], // near
            matrix[3] - matrix[2],  // far
        )

        for (i in 0 until PLANES) {
            val plane = planes[i]
            val length = 1.0f / sqrt(plane.x * plane.x + plane.y * plane.y + plane.z * plane.z)
            planes[i] = Vec4f(plane.x * length, plane.y * length, plane.z * length, plane.w * length)
        }

        return planes
    }

    fun Array<Vec4f>.pack(): FloatArray {
        val output = FloatArray(this.size * Vec4f.LENGTH)
        for ((index, vec) in this.withIndex()) {
            output[index * Vec4f.LENGTH + 0] = vec.x
            output[index * Vec4f.LENGTH + 1] = vec.y
            output[index * Vec4f.LENGTH + 2] = vec.z
            output[index * Vec4f.LENGTH + 3] = vec.w
        }

        return output
    }
}
