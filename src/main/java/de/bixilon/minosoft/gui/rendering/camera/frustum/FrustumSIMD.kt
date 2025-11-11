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
import de.bixilon.minosoft.gui.rendering.camera.frustum.FrustumUtil.PLANES
import de.bixilon.minosoft.gui.rendering.camera.frustum.FrustumUtil.pack
import de.bixilon.minosoft.util.SIMDUtil
import jdk.incubator.vector.FloatVector
import jdk.incubator.vector.VectorMask
import jdk.incubator.vector.VectorOperators

class FrustumSIMD(
    val planes: FloatArray,
    val signs: BooleanArray,
) : Frustum {

    init {
        assert(SIMDUtil.SUPPORTED_JDK) { "Your JDK does not support SIMD" }
        assert(FloatVector.SPECIES_PREFERRED.length() >= 4) { "The Frustum requires at least 128bit float registers!" }
    }

    override fun containsSphere(x: Float, y: Float, z: Float, radius: Float): Boolean {
        val point = FloatVector.fromArray(FloatVector.SPECIES_128, floatArrayOf(x, y, z, 1.0f), 0)

        for (index in 0 until PLANES) {
            val offset = index * Vec4f.LENGTH

            val plane = FloatVector.fromArray(FloatVector.SPECIES_128, this.planes, offset)

            val sum = plane.mul(point).reduceLanes(VectorOperators.ADD)

            if (sum < -radius) return false
        }
        return true
    }

    override fun containsAABB(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float): Boolean {
        val min = FloatVector.broadcast(FloatVector.SPECIES_128, 1.0f)
            .withLane(0, minX)
            .withLane(1, minY)
            .withLane(2, minZ)

        val max = FloatVector.broadcast(FloatVector.SPECIES_128, 1.0f)
            .withLane(0, maxX)
            .withLane(1, maxY)
            .withLane(2, maxZ)

        for (index in 0 until PLANES) {
            val offset = index * Vec4f.LENGTH

            val plane = FloatVector.fromArray(FloatVector.SPECIES_128, this.planes, offset)
            val mask = VectorMask.fromArray(FloatVector.SPECIES_128, this.signs, offset)

            val point = min.blend(max, mask)


            val sum = plane.mul(point).reduceLanes(VectorOperators.ADD)


            if (sum < 0.0f) return false
        }

        return true
    }

    companion object {


        fun calculate(matrix: Mat4f): FrustumSIMD {
            val planes = FrustumUtil.calculatePlanes(matrix)

            val array = planes.pack()

            val signs = BooleanArray(planes.size * 4)

            for (index in array.indices) {
                signs[index] = array[index] >= 0.0f
            }

            return FrustumSIMD(array, signs)
        }
    }
}
