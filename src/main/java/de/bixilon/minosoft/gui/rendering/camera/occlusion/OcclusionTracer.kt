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
import de.bixilon.minosoft.data.world.World.Companion.MAX_VERTICAL_VIEW_DISTANCE
import de.bixilon.minosoft.data.world.chunk.ChunkUtil.isInViewDistance
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.container.block.occlusion.OcclusionState
import de.bixilon.minosoft.data.world.container.block.occlusion.SectionOcclusion
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

    init {
        assert(viewDistance >= 2)
    }

    // allow tracing from above or below the world
    private val minSection = maxOf(dimension.minSection, position.y - MAX_VERTICAL_VIEW_DISTANCE) - 1
    private val maxSection = minOf(dimension.maxSection, position.y + MAX_VERTICAL_VIEW_DISTANCE) + 1

    // TODO: reuse those (reduce allocations)
    private val culled = SectionPositionSet(chunkPosition, viewDistance, minSection, maxSection)
    private val visible = SectionPositionSet(chunkPosition, viewDistance, minSection, maxSection)
    private val paths = Array(Axes.VALUES.size) { SectionPositionSet(chunkPosition, viewDistance, minSection, maxSection) }

    private var free = FREE_SIZE
    val queue: HashSet<SectionOcclusion> = HashSet(MAX_QUEUE_SIZE)


    private inline fun isInViewDistance(chunk: Chunk): Boolean {
        if (!chunk.position.isInViewDistance(viewDistance, this.chunkPosition)) return false
        return true
    }

    private fun trace(chunk: Chunk, height: SectionHeight, direction: Directions, vector1: SVec3i, vector2: SVec3i, vector: SVec3i) {
        if (!isInViewDistance(chunk)) return
        if (height < minSection || height > maxSection) return

        val position = SectionPosition.of(chunk.position, height)

        if (position in culled) return
        if (position in paths[direction.axis.ordinal]) return // path from same source direction already taken

        if (position !in visible && position !in frustum) {
            culled += position
            return
        }

        if (vector1 != SVec3i.EMPTY) { // check if we are too far away from a straight line to the camera
            val cross = vector1.cross(vector)

            val distance2 = cross.length2().toFloat() / vector.length2().toFloat()

            if (distance2 > 1.2f * 1.2f) return
        }

        paths[direction.axis.ordinal] += position
        visible += position

        val inverted = direction.inverted
        val neighbours = chunk.neighbours
        val occlusion = chunk[height]?.blocks?.occlusion // TODO: empty section bypass?


        if (vector.x <= 0) trace(occlusion, neighbours, height, inverted, Directions.WEST, vector2, vector)
        if (vector.x >= 0) trace(occlusion, neighbours, height, inverted, Directions.EAST, vector2, vector)

        if (vector.z <= 0) trace(occlusion, neighbours, height, inverted, Directions.NORTH, vector2, vector)
        if (vector.z >= 0) trace(occlusion, neighbours, height, inverted, Directions.SOUTH, vector2, vector)

        if (vector.y <= 0) trace(occlusion, chunk, height, inverted, Directions.DOWN, vector2, vector)
        if (vector.y >= 0) trace(occlusion, chunk, height, inverted, Directions.UP, vector2, vector)
    }

    private inline fun trace(occlusion: SectionOcclusion?, neighbours: ChunkNeighbours, height: SectionHeight, source: Directions, destination: Directions, vector2: SVec3i, vector: SVec3i) {
        if (occlusion != null && occlusion.isOccludedQueue(source, destination)) return
        val next = neighbours[destination] ?: return
        trace(next, height, destination, vector2, vector, vector + destination)
    }

    private inline fun trace(occlusion: SectionOcclusion?, chunk: Chunk, height: SectionHeight, source: Directions, destination: Directions, vector2: SVec3i, vector: SVec3i) {
        if (occlusion != null && occlusion.isOccludedQueue(source, destination)) return
        trace(chunk, height + destination.vector.y, destination, vector2, vector, vector + destination)
    }

    private fun SectionOcclusion.isOccludedQueue(source: Directions, destination: Directions): Boolean {
        if (this.state == OcclusionState.INVALID) {
            calculateFast() // maybe it is possible without effort
            if (this.state == OcclusionState.INVALID) {
                if (free > 0) {
                    calculate()
                    free--
                } else {
                    if (queue.size > MAX_QUEUE_SIZE) return true
                    queue += this
                    return true
                }
            }
        }

        return isOccluded(source, destination)
    }

    fun trace(chunk: Chunk): OcclusionGraph {
        for (direction in Directions) {
            val neighbour = if (direction.axis == Axes.Y) chunk else chunk.neighbours[direction] ?: continue
            trace(neighbour, position.y + direction.vector.y, direction, SVec3i(), SVec3i(), SVec3i(direction.vector))
        }

        visible += position

        return OcclusionGraph(visible)
    }

    companion object {
        const val FREE_SIZE = 30
        const val MAX_QUEUE_SIZE = 200

        fun Set<SectionOcclusion>.calculate() = this.forEach(SectionOcclusion::calculate)
    }
}
