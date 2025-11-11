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
import de.bixilon.minosoft.util.SIMDUtil
import jdk.incubator.vector.FloatVector

interface Frustum {

    fun containsSphere(x: Float, y: Float, z: Float, radius: Float): Boolean
    fun containsAABB(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float): Boolean


    companion object {

        fun create(matrix: Mat4f) = when {
            !SIMDUtil.SUPPORTED_JDK -> FrustumScalar.calculate(matrix)
            FloatVector.SPECIES_PREFERRED.length() >= 4 -> FrustumSIMD.calculate(matrix)
            else -> FrustumScalar.calculate(matrix)
        }
    }
}
