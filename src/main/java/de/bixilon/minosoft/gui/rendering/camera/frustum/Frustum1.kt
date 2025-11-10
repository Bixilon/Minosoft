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

import de.bixilon.kmath.mat.mat3.f.Mat3f
import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec4.f.Vec4f
import de.bixilon.kutil.collections.CollectionUtil.get
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum

// Big thanks to: https://gist.github.com/podgorskiy/e698d18879588ada9014768e3e82a644
class Frustum1(
    val normals: Array<Vec3f>,
    val planes: Array<Vec4f>,
) : Frustum {

    private fun Vec4f.dot(x: Float, y: Float, z: Float) = this.x * x + this.y * y + this.z * z + this.w

    override fun containsRegion(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float): Frustum.FrustumResult {
        for (i in 0 until Planes.SIZE) {
            val plane = planes[i]
            if (plane.dot(minX, minY, minZ) < 0.0f
                && plane.dot(maxX, maxY, maxZ) < 0.0f // check max as 2nd, likely to be false
                && plane.dot(maxX, minY, minZ) < 0.0f
                && plane.dot(minX, maxY, minZ) < 0.0f
                && plane.dot(maxX, maxY, minZ) < 0.0f
                && plane.dot(minX, minY, maxZ) < 0.0f
                && plane.dot(maxX, minY, maxZ) < 0.0f
                && plane.dot(minX, maxY, maxZ) < 0.0f
            ) {
                return Frustum.FrustumResult.OUTSIDE
            }
        }

        for (i in 0 until 8) {
            val normal = normals[i]
            if (normal.x >= minX) return Frustum.FrustumResult.INSIDE
            if (normal.x <= maxX) return Frustum.FrustumResult.INSIDE

            if (normal.y >= minY) return Frustum.FrustumResult.INSIDE
            if (normal.y <= maxY) return Frustum.FrustumResult.INSIDE

            if (normal.z >= minZ) return Frustum.FrustumResult.INSIDE
            if (normal.z <= maxZ) return Frustum.FrustumResult.INSIDE
        }

        return Frustum.FrustumResult.OUTSIDE
    }

    companion object {

        fun calculate(matrix: Mat4f): Frustum1 {
            val planes = arrayOf(
                matrix[3] + matrix[0],
                matrix[3] - matrix[0],

                matrix[3] + matrix[1],
                matrix[3] - matrix[1],

                matrix[3] + matrix[2],
                matrix[3] - matrix[2],
            )
            val planesVec3 = arrayOf(
                planes[0].xyz,
                planes[1].xyz,

                planes[2].xyz,
                planes[3].xyz,

                planes[4].xyz,
                planes[5].xyz,
            )

            val crosses = arrayOf(
                planesVec3[Planes.LEFT] cross planesVec3[Planes.RIGHT],
                planesVec3[Planes.LEFT] cross planesVec3[Planes.BOTTOM],
                planesVec3[Planes.LEFT] cross planesVec3[Planes.TOP],
                planesVec3[Planes.LEFT] cross planesVec3[Planes.NEAR],
                planesVec3[Planes.LEFT] cross planesVec3[Planes.FAR],

                planesVec3[Planes.RIGHT] cross planesVec3[Planes.BOTTOM],
                planesVec3[Planes.RIGHT] cross planesVec3[Planes.TOP],
                planesVec3[Planes.RIGHT] cross planesVec3[Planes.NEAR],
                planesVec3[Planes.RIGHT] cross planesVec3[Planes.FAR],

                planesVec3[Planes.BOTTOM] cross planesVec3[Planes.TOP],
                planesVec3[Planes.BOTTOM] cross planesVec3[Planes.NEAR],
                planesVec3[Planes.BOTTOM] cross planesVec3[Planes.FAR],

                planesVec3[Planes.TOP] cross planesVec3[Planes.NEAR],
                planesVec3[Planes.TOP] cross planesVec3[Planes.FAR],

                planesVec3[Planes.NEAR] cross planesVec3[Planes.FAR],
            )

            fun ij2k(i: Planes, j: Planes): Int {
                return i.ordinal * (9 - i.ordinal) / 2 + j.ordinal - 1
            }

            fun intersections(a: Planes, b: Planes, c: Planes): Vec3f {
                val d = planesVec3[a] dot crosses[ij2k(b, c)]
                val res = Mat3f(crosses[ij2k(b, c)], -crosses[ij2k(a, c)], crosses[ij2k(a, b)]) * Vec3f(planes[a].w, planes[b].w, planes[c].w)
                return res * (-1.0f / d)
            }

            val normals = arrayOf(
                intersections(Planes.LEFT, Planes.BOTTOM, Planes.NEAR),
                intersections(Planes.LEFT, Planes.TOP, Planes.NEAR),
                intersections(Planes.RIGHT, Planes.BOTTOM, Planes.NEAR),
                intersections(Planes.RIGHT, Planes.TOP, Planes.NEAR),

                intersections(Planes.LEFT, Planes.BOTTOM, Planes.FAR),
                intersections(Planes.LEFT, Planes.TOP, Planes.FAR),
                intersections(Planes.RIGHT, Planes.BOTTOM, Planes.FAR),
                intersections(Planes.RIGHT, Planes.TOP, Planes.FAR),
            )

            return Frustum1(normals, planes)
        }
    }


    private enum class Planes {
        LEFT,
        RIGHT,
        BOTTOM,
        TOP,
        NEAR,
        FAR,
        ;

        companion object : ValuesEnum<Planes> {
            const val SIZE = 6
            override val VALUES: Array<Planes> = values()
            override val NAME_MAP: Map<String, Planes> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
