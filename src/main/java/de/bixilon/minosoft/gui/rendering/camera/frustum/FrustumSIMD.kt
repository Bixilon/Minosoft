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
import de.bixilon.minosoft.util.SIMDUtil
import jdk.incubator.vector.FloatVector
import kotlin.math.sqrt

class FrustumSIMD(
    val planes: Array<Vec4f>,
) : Frustum {

    init {
        FloatVector.SPECIES_PREFERRED
        assert(SIMDUtil.SUPPORTED_JDK) { "Your JDK does not support SIMD" }
        assert(FloatVector.SPECIES_PREFERRED.length() >= 4) { "The Frustum requires at least 128bit float registers!" }
    }

    private fun Vec4f.dot(x: Float, y: Float, z: Float) = this.x * x + this.y * y + this.z * z + this.w

    override fun containsSphere(x: Float, y: Float, z: Float, radius: Float): Boolean {
        for (index in 0 until planes.size) {
            val plane = planes[index]

            if (plane.dot(x, y, z) < -radius) return false
        }
        return true
    }

    override fun containsAABB(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float): Boolean {
        //val planes = FloatArray(planes.size * 4)
        //for((index, plane) in this.planes.withIndex()) {
        //    planes[index * 4 + 0] = plane.x
        //    planes[index * 4 + 1] = plane.y
        //    planes[index * 4 + 2] = plane.z
        //    planes[index * 4 + 3] = plane.w
        //}
        //val min = floatArrayOf(minX, minY, minZ, 1.0f)
        //val max = floatArrayOf(maxX, maxY, maxZ, 1.0f)

        for (i in 0 until planes.size) {
            val plane = planes[i]

            val x = if (plane.x >= 0.0f) maxX else minX
            val y = if (plane.y >= 0.0f) maxY else minY
            val z = if (plane.z >= 0.0f) maxZ else minZ

            val a = floatArrayOf(plane.x, plane.y, plane.z, plane.w)
            val b = floatArrayOf(x, y, z, 1.0f)


            val v1 = FloatVector.fromArray(FloatVector.SPECIES_128, a, 0)
            val v2 = FloatVector.fromArray(FloatVector.SPECIES_128, b, 0)

            val mul = v1.mul(v2)


            if (mul.toArray().sum() < 0.0f) return false
        }
        return true
    }

    companion object {

        fun calculate(matrix: Mat4f): FrustumSIMD {
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

            return FrustumSIMD(planes)
        }
    }
}
