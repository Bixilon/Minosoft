/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.input.camera


import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.KUtil.get
import de.bixilon.minosoft.util.enum.ValuesEnum
import glm_.mat3x3.Mat3
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import glm_.vec4.Vec4d

// Bit thanks to: https://gist.github.com/podgorskiy/e698d18879588ada9014768e3e82a644
class Frustum(private val camera: Camera) {
    private var normals: List<Vec3> = listOf()
    private var planes: List<Vec4d> = listOf()

    init {
        recalculate()
    }

    fun recalculate() {
        val matrix = camera.viewProjectionMatrix.transpose()
        val planes = listOf(
            matrix[3] + matrix[0],
            matrix[3] - matrix[0],

            matrix[3] + matrix[1],
            matrix[3] - matrix[1],

            matrix[3] + matrix[2],
            matrix[3] - matrix[2],
        )

        val crosses = listOf(
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

        val normals: List<Vec3> = listOf(
            intersections(Planes.LEFT, Planes.BOTTOM, Planes.NEAR),
            intersections(Planes.LEFT, Planes.TOP, Planes.NEAR),
            intersections(Planes.RIGHT, Planes.BOTTOM, Planes.NEAR),
            intersections(Planes.RIGHT, Planes.TOP, Planes.NEAR),

            intersections(Planes.LEFT, Planes.BOTTOM, Planes.FAR),
            intersections(Planes.LEFT, Planes.TOP, Planes.FAR),
            intersections(Planes.RIGHT, Planes.BOTTOM, Planes.FAR),
            intersections(Planes.RIGHT, Planes.TOP, Planes.FAR),
        )

        synchronized(this.normals) {
            this.normals = normals
        }

        synchronized(this.planes) {
            this.planes = planes
        }
    }


    private fun containsRegion(min: Vec3, max: Vec3): Boolean {
        if (!RenderConstants.FRUSTUM_CULLING_ENABLED) {
            return true
        }

        val normals: List<Vec3>
        synchronized(this.normals) {
            normals = this.normals
        }
        val planes: List<Vec4d>
        synchronized(this.planes) {
            planes = this.planes
        }

        for (i in 0 until Planes.VALUES.size) {
            if (
                (planes[i] dot Vec4d(min.x, min.y, min.z, 1.0f)) < 0.0f
                && (planes[i] dot Vec4d(max.x, min.y, min.z, 1.0f)) < 0.0f
                && (planes[i] dot Vec4d(min.x, max.y, min.z, 1.0f)) < 0.0f
                && (planes[i] dot Vec4d(max.x, max.y, min.z, 1.0f)) < 0.0f
                && (planes[i] dot Vec4d(min.x, min.y, max.z, 1.0f)) < 0.0f
                && (planes[i] dot Vec4d(max.x, min.y, max.z, 1.0f)) < 0.0f
                && (planes[i] dot Vec4d(min.x, max.y, max.z, 1.0f)) < 0.0f
                && (planes[i] dot Vec4d(max.x, max.y, max.z, 1.0f)) < 0.0f
            ) {
                return false
            }
        }

        fun checkPoint(check: (Vec3) -> Boolean): Boolean {
            var out = 0
            for (i in 0 until 8) {
                if (check(normals[i])) {
                    out++
                }
            }
            return out == 8
        }

        val checks: List<(Vec3) -> Boolean> = listOf(
            { it.x > max.x },
            { it.x < min.x },

            { it.y > max.y },
            { it.y < min.y },

            { it.z > max.z },
            { it.z < min.z },
        )

        for (check in checks) {
            if (checkPoint(check)) {
                return false
            }
        }

        return true
    }

    fun containsChunk(chunkPosition: Vec2i, sectionHeight: Int, minPosition: Vec3i, maxPosition: Vec3i): Boolean {
        val from = Vec3i.of(chunkPosition, sectionHeight, minPosition)
        val to = Vec3i.of(chunkPosition, sectionHeight, maxPosition + 1)
        return containsRegion(Vec3(from), Vec3(to))
    }

    fun containsAABB(aabb: AABB): Boolean {
        return containsRegion(Vec3(aabb.min), Vec3(aabb.max))
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
            override val VALUES: Array<Planes> = values()
            override val NAME_MAP: Map<String, Planes> = KUtil.getEnumValues(VALUES)
        }
    }
}
