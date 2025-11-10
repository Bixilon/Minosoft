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


import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.camera.MatrixHandler

class FrustumCulling(
    private val camera: Camera,
    private val matrixHandler: MatrixHandler,
    private val world: World,
) {
    private var frustum: Frustum? = null
    var revision = 0
        private set

    fun recalculate() {
        frustum = Frustum1.calculate(matrixHandler.viewProjectionMatrix)
        revision++
    }

    private fun containsRegion(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float): Boolean {
        val first = frustum?.containsRegion(minX, minY, minZ, maxX, maxY, maxZ) ?: Frustum.FrustumResult.MAYBE

        return when (first) {
            Frustum.FrustumResult.INSIDE -> true
            Frustum.FrustumResult.OUTSIDE -> false
            else -> true
        }
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

    companion object {
        val SECTION_MIN_POSITION = InSectionPosition(0, 0, 0)
        val SECTION_MAX_POSITION = InSectionPosition(ChunkSize.SECTION_MAX_X, ChunkSize.SECTION_MAX_Y, ChunkSize.SECTION_MAX_Z)
    }
}
