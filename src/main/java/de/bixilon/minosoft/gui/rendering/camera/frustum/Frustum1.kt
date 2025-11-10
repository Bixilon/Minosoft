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

// Big thanks to: https://gist.github.com/podgorskiy/e698d18879588ada9014768e3e82a644
class Frustum1(
    val planes: Array<Vec4f>,
) : Frustum {

    private fun Vec4f.dot(x: Float, y: Float, z: Float) = this.x * x + this.y * y + this.z * z + this.w

    override fun containsSphere(x: Float, y: Float, z: Float, radius: Float): Boolean {
        for (index in 0 until planes.size) {
            val plane = planes[index]

            if (plane.dot(x, y, z) < -radius) return false
        }
        return true
    }

    override fun containsAABB(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float): Boolean {
        for (i in 0 until planes.size) {
            val plane = planes[i]

            val x = if (plane.x >= 0.0f) maxX else minX
            val y = if (plane.y >= 0.0f) maxY else minY
            val z = if (plane.z >= 0.0f) maxZ else minZ

            if (plane.dot(x, y, z) < 0.0f) return false
        }
        return true
    }

    companion object {

        fun calculate(matrix: Mat4f): Frustum1 {
            val planes = arrayOf(
                matrix[3] + matrix[0], // left
                matrix[3] - matrix[0], // right

                matrix[3] + matrix[1], // bottom
                matrix[3] - matrix[1], // top

                matrix[3] + matrix[2], // near
                matrix[3] - matrix[2],  // far
            )

            for (i in 0 until planes.size) {
                val plane = planes[i]
                val length = 1.0f / sqrt(plane.x * plane.x + plane.y * plane.y + plane.z * plane.z)
                planes[i] = Vec4f(plane.x * length, plane.y * length, plane.z * length, plane.w * length)
            }

            return Frustum1(planes)
        }
    }
}
