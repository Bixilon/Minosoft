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

// Big thanks to: https://gist.github.com/podgorskiy/e698d18879588ada9014768e3e82a644
class FrustumScalar(
    val planes: FloatArray,
) : Frustum {

    private inline fun FloatArray.dot(offset: Int, x: Float, y: Float, z: Float) = this[offset + 0] * x + this[offset + 1] * y + this[offset + 2] * z + this[offset + 3]

    override fun containsSphere(x: Float, y: Float, z: Float, radius: Float): Boolean {
        for (index in 0 until PLANES) {
            val offset = index * Vec4f.LENGTH

            if (this.planes.dot(offset, x, y, z) < -radius) return false
        }
        return true
    }

    override fun containsAABB(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float): Boolean {
        for (index in 0 until PLANES) {
            val offset = index * Vec4f.LENGTH

            val x = if (planes[offset + 0] >= 0.0f) maxX else minX
            val y = if (planes[offset + 1] >= 0.0f) maxY else minY
            val z = if (planes[offset + 2] >= 0.0f) maxZ else minZ

            if (this.planes.dot(offset, x, y, z) < 0.0f) return false
        }

        return true
    }

    companion object {

        fun calculate(matrix: Mat4f): FrustumScalar {
            val planes = FrustumUtil.calculatePlanes(matrix)

            return FrustumScalar(planes.pack())
        }
    }
}
