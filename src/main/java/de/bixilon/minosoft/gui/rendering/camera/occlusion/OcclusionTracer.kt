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

package de.bixilon.minosoft.gui.rendering.camera.occlusion

import de.bixilon.kmath.vec.vec3.i.SVec3i
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.chunk.ChunkUtil.isInViewDistance
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbourArray
import de.bixilon.minosoft.data.world.container.block.SectionOcclusion
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.camera.Camera

class OcclusionTracer(
    val position: SectionPosition,
    dimension: DimensionProperties,
    camera: Camera,
    val viewDistance: Int,
) {
    val chunkPosition = position.chunkPosition
    private val frustum = camera.frustum

    // allow tracing from above or below the world
    private val minSection = dimension.minSection - 1
    private val maxSection = dimension.maxSection + 1

    private val skip = SectionPositionSet(chunkPosition, viewDistance, minSection, dimension.sections + 2) // TODO: reuse
    private val visible = SectionPositionSet(chunkPosition, viewDistance, minSection, dimension.sections + 2)
    private val paths = Array(Axes.VALUES.size) { SectionPositionSet(chunkPosition, viewDistance, minSection, dimension.sections + 2) }


    private inline fun isInViewDistance(chunk: Chunk): Boolean {
        if (!chunk.position.isInViewDistance(viewDistance, this.chunkPosition)) return false
        return true
    }

    private fun trace(chunk: Chunk, height: SectionHeight, direction: Directions, vector: SVec3i) {
        // TODO: keep track of direction and don't allow going of the axis too far (we can not bend our look direction). This will hide caves and reveans too if they are occluded
        if (!isInViewDistance(chunk)) return
        if (height < minSection || height > maxSection) return

        val position = SectionPosition.of(chunk.position, height)

        if (position in skip) return
        if (position in paths[direction.axis.ordinal]) return // path from same source direction already taken

        val section = chunk[height]
        if (!frustum.containsChunkSection(position)) {
            skip += position
            return
        }
        paths[direction.axis.ordinal] += position
        visible += position


        val inverted = direction.inverted
        val neighbours = chunk.neighbours.neighbours
        val occlusion = section?.blocks?.occlusion // TODO: empty section bypass?


        if (vector.x <= 0) trace(occlusion, neighbours, height, inverted, Directions.WEST, vector)
        if (vector.x >= 0) trace(occlusion, neighbours, height, inverted, Directions.EAST, vector)

        if (vector.z <= 0) trace(occlusion, neighbours, height, inverted, Directions.NORTH, vector)
        if (vector.z >= 0) trace(occlusion, neighbours, height, inverted, Directions.SOUTH, vector)

        if (vector.y <= 0) trace(occlusion, chunk, height, inverted, Directions.DOWN, vector)
        if (vector.y >= 0) trace(occlusion, chunk, height, inverted, Directions.UP, vector)
    }

    private inline fun trace(occlusion: SectionOcclusion?, neighbours: ChunkNeighbourArray, height: SectionHeight, source: Directions, destination: Directions, vector: SVec3i) {
        if (occlusion != null && occlusion.isOccluded(source, destination)) return
        val next = neighbours[destination] ?: return
        trace(next, height, destination, vector + destination)
    }

    private inline fun trace(occlusion: SectionOcclusion?, chunk: Chunk, height: SectionHeight, source: Directions, destination: Directions, vector: SVec3i) {
        if (occlusion != null && occlusion.isOccluded(source, destination)) return
        trace(chunk, height + destination.vector.y, destination, vector + destination)
    }

    fun trace(chunk: Chunk): OcclusionGraph {
        for (direction in Directions) {
            val neighbour = if (direction.axis == Axes.Y) chunk else chunk.neighbours[direction] ?: continue
            trace(neighbour, position.y + direction.vector.y, direction, SVec3i(direction.vector))
        }

        visible += position

        return OcclusionGraph(visible)
    }
}
