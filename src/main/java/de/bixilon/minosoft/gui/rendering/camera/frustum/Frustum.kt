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
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.camera.MatrixHandler

// Big thanks to: https://gist.github.com/podgorskiy/e698d18879588ada9014768e3e82a644
class Frustum(
    private val camera: Camera,
    private val matrixHandler: MatrixHandler,
    private val world: World,
) {
    private var data: FrustumData? = null
    var revision = 0
        private set

    private fun recalculate(matrix: Mat4f) {
        val matrix = matrix.transpose()
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

        this.data = FrustumData(normals, planes)
        revision++
    }

    fun recalculate() {
        recalculate(matrixHandler.viewProjectionMatrix.transpose())
    }

    fun Vec4f.dot(x: Float, y: Float, z: Float) = this.x * x + this.y * y + this.z * z + this.w


    private fun containsRegion(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float): Boolean {
        if (!RenderConstants.FRUSTUM_CULLING_ENABLED) return true
        val (normals, planes) = this.data ?: return true

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
                return false
            }
        }

        for (i in 0 until 8) {
            val normal = normals[i]
            if (normal.x >= minX) return true
            if (normal.x <= maxX) return true

            if (normal.y >= minY) return true
            if (normal.y <= maxY) return true

            if (normal.z >= minZ) return true
            if (normal.z <= maxZ) return true
        }

        return false
    }

    private fun containsRegion(min: Vec3f, max: Vec3f): Boolean {
        return containsRegion(min.x, min.y, min.z, max.x, max.y, max.z)
    }

    fun containsChunkSection(section: ChunkSection) = containsChunkSection(SectionPosition.of(section.chunk.position, section.height), section.blocks.minPosition, section.blocks.maxPosition)

    fun containsChunkSection(position: SectionPosition, minPosition: InSectionPosition = SECTION_MIN_POSITION, maxPosition: InSectionPosition = SECTION_MAX_POSITION): Boolean {
        val base = BlockPosition.of(position) - camera.offset.offset
        val min = Vec3f(base + minPosition)
        val max = Vec3f(base + maxPosition + 1)
        return containsRegion(min, max)
    }

    fun containsChunk(position: ChunkPosition): Boolean {
        val dimension = world.dimension

        val offset = camera.offset.offset
        val baseX = (position.x * ChunkSize.SECTION_WIDTH_X - offset.x).toFloat()
        val baseZ = (position.z * ChunkSize.SECTION_WIDTH_Z - offset.z).toFloat()

        val minY = (dimension.minY - offset.y).toFloat()
        val maxY = (dimension.maxY - offset.y).toFloat()

        return containsRegion(
            baseX, minY, baseZ,
            baseX + ChunkSize.SECTION_WIDTH_X, maxY, baseZ + ChunkSize.SECTION_WIDTH_Z,
        )
    }

    fun containsRegion(min: BlockPosition, max: BlockPosition): Boolean {
        val offset = camera.offset.offset
        return containsRegion(Vec3f(min - offset), Vec3f(max - offset))
    }

    fun containsAABB(aabb: AABB): Boolean {
        val offset = camera.offset.offset
        return containsRegion(
            (aabb.min.x - offset.x).toFloat(), (aabb.min.y - offset.y).toFloat(), (aabb.min.z - offset.z).toFloat(),
            (aabb.max.x - offset.x).toFloat(), (aabb.max.y - offset.y).toFloat(), (aabb.max.z - offset.z).toFloat(),
        )
    }

    operator fun contains(aabb: AABB) = containsAABB(aabb)
    operator fun contains(chunk: Chunk) = containsChunk(chunk.position)
    operator fun contains(position: ChunkPosition) = containsChunk(position)
    operator fun contains(section: ChunkSection) = containsChunkSection(section)

    private data class FrustumData(val normals: Array<Vec3f>, val planes: Array<Vec4f>)

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

    companion object {
        val SECTION_MIN_POSITION = InSectionPosition(0, 0, 0)
        val SECTION_MAX_POSITION = InSectionPosition(ChunkSize.SECTION_MAX_X, ChunkSize.SECTION_MAX_Y, ChunkSize.SECTION_MAX_Z)
    }
}
