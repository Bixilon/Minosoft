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

package de.bixilon.minosoft.gui.rendering.world.view

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.chunk.ChunkUtil.isInViewDistance
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class WorldVisibilityGraph(
    private val renderWindow: RenderWindow,
    private val camera: Camera,
) {
    private val connection = renderWindow.connection
    private val frustum = camera.matrixHandler.frustum
    private var cameraChunkPosition = Vec2i.EMPTY
    private var cameraSectionHeight = 0
    private var viewDistance = connection.world.view.viewDistance

    var minSection = 0
    var maxSection = 16

    private var visibilityLock = SimpleLock()
    private var visibilities: HashMap<Vec2i, IntOpenHashSet> = HashMap()

    // check for view distance (hide chunks that are far away)
    // check if direction is non-negative (i.e. basic frustum culling)
    // always show current chunk section
    // check occlusion culling
    // real frustum culling


    // occlusion culling
    // always show current section

    init {
        calculateGraph()
    }

    fun isChunkVisible(chunkPosition: Vec2i): Boolean {
        if (!chunkPosition.isInViewDistance(connection.world.view.viewDistance, cameraChunkPosition)) {
            return false
        }

        // ToDo: basic frustum culling
        return true
    }

    fun isSectionVisible(chunkPosition: Vec2i, sectionHeight: Int, minPosition: Vec3i = DEFAULT_MIN_POSITION, maxPosition: Vec3i = DEFAULT_MAX_POSITION, checkChunk: Boolean = true): Boolean {
        if (checkChunk && !isChunkVisible(chunkPosition)) {
            return false
        }
        if (chunkPosition == cameraChunkPosition && sectionHeight == cameraSectionHeight) { // ToDo: Remove duplicated chunk position check
            return true
        }

        if (!frustum.containsChunk(chunkPosition, sectionHeight, minPosition, maxPosition)) {
            return false
        }
        visibilityLock.acquire()
        val visible = this.visibilities[chunkPosition]?.contains(sectionHeight)
        visibilityLock.release()

        return visible == true
    }

    fun updateCamera(chunkPosition: Vec2i, sectionHeight: Int) {
        if (this.cameraChunkPosition == chunkPosition && this.cameraSectionHeight == sectionHeight) {
            return
        }
        this.cameraChunkPosition = chunkPosition
        this.cameraSectionHeight = sectionHeight
        this.minSection = connection.world.dimension?.lowestSection ?: 0
        this.maxSection = connection.world.dimension?.highestSection ?: 16
        calculateGraph()
    }

    private fun checkSection(graph: MutableMap<Vec2i, MutableSet<VisitStatus>>, chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, visibilities: MutableSet<VisitStatus>, direction: Directions, directionVector: Vec3i, steps: Int, ignoreVisibility: Boolean = false) {
        if (steps > 64 * 3) {
            Log.log(LogMessageType.OTHER, LogLevels.WARN) { "Potential stack overflow: $chunkPosition:$sectionHeight $direction $directionVector" }
            return
        }
        val inverted = direction.inverted
        val nextStep = steps + 1

        if (ignoreVisibility) {
            visibilities += VisitStatus(sectionHeight, Directions.DOWN, Directions.DOWN)
        }

        if (sectionHeight > minSection && directionVector.y <= 0 && (ignoreVisibility || chunk[sectionHeight]?.blocks?.isOccluded(inverted, Directions.DOWN) != true)) {
            val status = VisitStatus(sectionHeight - 1, inverted, Directions.DOWN)
            if (status !in visibilities) {
                visibilities += status
                checkSection(graph, chunkPosition, sectionHeight - 1, chunk, visibilities, Directions.DOWN, Vec3i(directionVector).apply { y = -1 }, nextStep)
            }
        }
        if (sectionHeight < maxSection && directionVector.y >= 0 && (ignoreVisibility || chunk[sectionHeight]?.blocks?.isOccluded(inverted, Directions.UP) != true)) {
            val status = VisitStatus(sectionHeight + 1, inverted, Directions.UP)
            if (status !in visibilities) {
                visibilities += status
                checkSection(graph, chunkPosition, sectionHeight + 1, chunk, visibilities, Directions.UP, Vec3i(directionVector).apply { y = 1 }, nextStep)
            }
        }


        if (directionVector.x <= 0 && isChunkVisible(chunkPosition) && (ignoreVisibility || chunk[sectionHeight]?.blocks?.isOccluded(inverted, Directions.NORTH) != true)) {
            val nextPosition = chunkPosition + direction
            val nextChunk = connection.world.chunks.original[nextPosition]
            if (nextChunk != null) {
                val nextVisibilities = graph.getOrPut(nextPosition) { HashSet() }
                val status = VisitStatus(sectionHeight, inverted, Directions.NORTH)
                if (status !in nextVisibilities) {
                    nextVisibilities += status
                    checkSection(graph, nextPosition, sectionHeight, nextChunk, nextVisibilities, Directions.NORTH, Vec3i(directionVector).apply { x = -1 }, nextStep)
                }
            }
        }
        if (directionVector.x >= 0 && isChunkVisible(chunkPosition) && (ignoreVisibility || chunk[sectionHeight]?.blocks?.isOccluded(inverted, Directions.SOUTH) != true)) {
            val nextPosition = chunkPosition + direction
            val nextChunk = connection.world.chunks.original[nextPosition]
            if (nextChunk != null) {
                val nextVisibilities = graph.getOrPut(nextPosition) { HashSet() }
                val status = VisitStatus(sectionHeight, inverted, Directions.SOUTH)
                if (status !in nextVisibilities) {
                    nextVisibilities += status
                    checkSection(graph, nextPosition, sectionHeight, nextChunk, nextVisibilities, Directions.SOUTH, Vec3i(directionVector).apply { x = 1 }, nextStep)
                }
            }
        }

        if (directionVector.z <= 0 && isChunkVisible(chunkPosition) && (ignoreVisibility || chunk[sectionHeight]?.blocks?.isOccluded(inverted, Directions.WEST) != true)) {
            val nextPosition = chunkPosition + direction
            val nextChunk = connection.world.chunks.original[nextPosition]
            if (nextChunk != null) {
                val nextVisibilities = graph.getOrPut(nextPosition) { HashSet() }
                val status = VisitStatus(sectionHeight, inverted, Directions.WEST)
                if (status !in nextVisibilities) {
                    nextVisibilities += status
                    checkSection(graph, nextPosition, sectionHeight, nextChunk, nextVisibilities, Directions.WEST, Vec3i(directionVector).apply { z = -1 }, nextStep)
                }
            }
        }
        if (directionVector.z >= 0 && isChunkVisible(chunkPosition) && (ignoreVisibility || chunk[sectionHeight]?.blocks?.isOccluded(inverted, Directions.EAST) != true)) {
            val nextPosition = chunkPosition + direction
            val nextChunk = connection.world.chunks.original[nextPosition]
            if (nextChunk != null) {
                val nextVisibilities = graph.getOrPut(nextPosition) { HashSet() }
                val status = VisitStatus(sectionHeight, inverted, Directions.EAST)
                if (status !in nextVisibilities) {
                    nextVisibilities += status
                    checkSection(graph, nextPosition, sectionHeight, nextChunk, nextVisibilities, Directions.EAST, Vec3i(directionVector).apply { z = 1 }, nextStep)
                }
            }
        }
    }

    private fun calculateGraph() {
        connection.world.chunks.lock.acquire()
        val start = TimeUtil.nanos
        println("Calculating graph...")

        val chunkPosition = cameraChunkPosition
        val sectionHeight = cameraSectionHeight
        this.viewDistance = connection.world.view.viewDistance

        val chunk = connection.world.chunks.original[chunkPosition]
        if (chunk == null) {
            connection.world.chunks.lock.release()
            return
        }

        val graph: MutableMap<Vec2i, MutableSet<VisitStatus>> = HashMap()

        for (direction in Directions.VALUES) {
            val nextPosition = chunkPosition + direction
            val nextChunk = connection.world.chunks.original[nextPosition] ?: continue
            val nextVisibility = graph.getOrPut(nextPosition) { HashSet() }
            checkSection(graph, nextPosition, sectionHeight, nextChunk, nextVisibility, direction, direction.vector, 0, true)
        }

        updateVisibilityGraph(graph)



        println("Done in ${(TimeUtil.nanos - start) / 1000}")

        connection.world.chunks.lock.release()
    }

    private fun updateVisibilityGraph(graph: MutableMap<Vec2i, MutableSet<VisitStatus>>) {
        visibilityLock.lock()
        this.visibilities.clear()


        for ((position, statuses) in graph) {
            val singleVisibility = this.visibilities.getOrPut(position) { IntOpenHashSet() }
            for (status in statuses) {
                singleVisibility += status.sectionHeight
            }
        }

        visibilityLock.unlock()
    }

    private data class VisitStatus(
        val sectionHeight: Int,
        val from: Directions,
        val to: Directions,
    )

    companion object {
        private val DEFAULT_MIN_POSITION = Vec3i.EMPTY
        private val DEFAULT_MAX_POSITION = Vec3i(ProtocolDefinition.SECTION_WIDTH_X, ProtocolDefinition.SECTION_HEIGHT_Y, ProtocolDefinition.SECTION_WIDTH_Z)
    }
}
