/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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


import de.bixilon.kotlinglm.mat3x3.Mat3
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kotlinglm.vec4.Vec4
import de.bixilon.kutil.collections.CollectionUtil.get
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.camera.MatrixHandler
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.util.vec.vec4.Vec4Util.dot

// Big thanks to: https://gist.github.com/podgorskiy/e698d18879588ada9014768e3e82a644
class Frustum(
    private val matrixHandler: MatrixHandler,
) {
    private lateinit var data: FrustumData

    fun recalculate() {
        val matrix = matrixHandler.viewProjectionMatrix.transpose()
        val planes = arrayOf(
            matrix[3] + matrix[0],
            matrix[3] - matrix[0],

            matrix[3] + matrix[1],
            matrix[3] - matrix[1],

            matrix[3] + matrix[2],
            matrix[3] - matrix[2],
        )

        val crosses = arrayOf(
            Vec3(planes[Planes.LEFT]) cross Vec3(planes[Planes.RIGHT]),
            Vec3(planes[Planes.LEFT]) cross Vec3(planes[Planes.BOTTOM]),
            Vec3(planes[Planes.LEFT]) cross Vec3(planes[Planes.TOP]),
            Vec3(planes[Planes.LEFT]) cross Vec3(planes[Planes.NEAR]),
            Vec3(planes[Planes.LEFT]) cross Vec3(planes[Planes.FAR]),

            Vec3(planes[Planes.RIGHT]) cross Vec3(planes[Planes.BOTTOM]),
            Vec3(planes[Planes.RIGHT]) cross Vec3(planes[Planes.TOP]),
            Vec3(planes[Planes.RIGHT]) cross Vec3(planes[Planes.NEAR]),
            Vec3(planes[Planes.RIGHT]) cross Vec3(planes[Planes.FAR]),

            Vec3(planes[Planes.BOTTOM]) cross Vec3(planes[Planes.TOP]),
            Vec3(planes[Planes.BOTTOM]) cross Vec3(planes[Planes.NEAR]),
            Vec3(planes[Planes.BOTTOM]) cross Vec3(planes[Planes.FAR]),

            Vec3(planes[Planes.TOP]) cross Vec3(planes[Planes.NEAR]),
            Vec3(planes[Planes.TOP]) cross Vec3(planes[Planes.FAR]),

            Vec3(planes[Planes.NEAR]) cross Vec3(planes[Planes.FAR]),
        )

        fun ij2k(i: Planes, j: Planes): Int {
            return i.ordinal * (9 - i.ordinal) / 2 + j.ordinal - 1
        }

        fun intersections(a: Planes, b: Planes, c: Planes): Vec3 {
            val d = Vec3(planes[a]) dot crosses[ij2k(b, c)]
            val res = Mat3(crosses[ij2k(b, c)], -crosses[ij2k(a, c)], crosses[ij2k(a, b)]) * Vec3(planes[a].w, planes[b].w, planes[c].w)
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

        this.data = FrustumData(normals, planes)
    }


    private fun containsRegion(min: Vec3, max: Vec3): Boolean {
        if (!RenderConstants.FRUSTUM_CULLING_ENABLED) {
            return true
        }
        val (normals, planes) = this.data
        val minArray = min.array
        val maxArray = max.array

        for (i in 0 until Planes.SIZE) {
            val plane = planes[i].array
            if (plane.dot(minArray[0], minArray[1], minArray[2]) < 0.0f
                && plane.dot(maxArray[0], maxArray[1], maxArray[2]) < 0.0f // check max as 2nd, likely to be false
                && plane.dot(maxArray[0], minArray[1], minArray[2]) < 0.0f
                && plane.dot(minArray[0], maxArray[1], minArray[2]) < 0.0f
                && plane.dot(maxArray[0], maxArray[1], minArray[2]) < 0.0f
                && plane.dot(minArray[0], minArray[1], maxArray[2]) < 0.0f
                && plane.dot(maxArray[0], minArray[1], maxArray[2]) < 0.0f
                && plane.dot(minArray[0], maxArray[1], maxArray[2]) < 0.0f
            ) {
                return false
            }
        }

        for (i in 0 until 8) {
            val normal = normals[i].array
            if (normal[0] >= min.x) return true
            if (normal[0] <= max.x) return true

            if (normal[1] >= min.y) return true
            if (normal[1] <= max.y) return true

            if (normal[2] >= min.z) return true
            if (normal[2] <= max.z) return true
        }

        return false
    }

    fun containsChunk(chunkPosition: Vec2i, sectionHeight: Int, minPosition: Vec3i, maxPosition: Vec3i): Boolean {
        val min = Vec3i.of(chunkPosition, sectionHeight, minPosition)
        val max = Vec3i.of(chunkPosition, sectionHeight, maxPosition + 1)
        return containsRegion(Vec3(min), Vec3(max))
    }

    fun containsRegion(min: Vec3i, max: Vec3i): Boolean {
        return containsRegion(Vec3(min), Vec3(max))
    }

    fun containsAABB(aabb: AABB): Boolean {
        return containsRegion(Vec3(aabb.min), Vec3(aabb.max))
    }

    private data class FrustumData(val normals: Array<Vec3>, val planes: Array<Vec4>)

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
