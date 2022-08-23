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

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.data.world.OcclusionUpdateCallback
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.modding.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.sectionHeight
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.chunk.ChunkUtil.isInViewDistance
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

/**
 * Handles visibility of objects
 *
 * Credit for occlusion culling:
 *  - https://github.com/stackotter/delta-client (with big thanks to @stackotter for ideas and explanation!)
 *  - https://tomcc.github.io/2014/08/31/visibility-1.html
 */
class WorldVisibilityGraph(
    private val renderWindow: RenderWindow,
    camera: Camera,
) : OcclusionUpdateCallback {
    private val connection = renderWindow.connection
    private val frustum = camera.matrixHandler.frustum
    private var cameraChunkPosition = Vec2i.EMPTY
    private var cameraSectionHeight = 0
    private var viewDistance = connection.world.view.viewDistance
    private val chunks = connection.world.chunks.unsafe
    private var lastFrustumRevision = -1


    private var recalculateNextFrame = false

    private var minSection = 0
    private var maxSection = 16
    private var maxIndex = 15
    private var sections = 16

    private var chunkMin = Vec2i.EMPTY
    private var chunkMax = Vec2i.EMPTY
    private var worldSize = Vec2i.EMPTY

    private var graph: Array<Array<BooleanArray?>?> = arrayOfNulls(0)

    private lateinit var frustumCache: Array<ByteArray?>

    // check for view distance (hide chunks that are far away)
    // check if direction is non-negative (i.e. basic frustum culling)
    // always show current chunk section
    // check occlusion culling
    // real frustum culling


    // occlusion culling
    // always show current section

    init {
        calculateGraph()
        connection.world.occlusionUpdateCallback = this
    }

    fun isInViewDistance(chunkPosition: Vec2i): Boolean {
        return chunkPosition.isInViewDistance(connection.world.view.viewDistance, cameraChunkPosition)
    }

    fun isChunkVisible(chunkPosition: Vec2i): Boolean {
        if (!isInViewDistance(chunkPosition)) {
            return false
        }
        if (!RenderConstants.OCCLUSION_CULLING_ENABLED) {
            return true
        }

        // ToDo: basic frustum culling
        return getChunkVisibility(chunkPosition) != null // ToDo: check if all values are false
    }

    private fun getChunkVisibility(chunkPosition: Vec2i): BooleanArray? {
        val x = chunkPosition.x - chunkMin.x
        val y = chunkPosition.y - chunkMin.y

        return this.graph.getOrNull(x)?.getOrNull(y)
    }

    fun isAABBVisible(aabb: AABB): Boolean {
        if (!RenderConstants.OCCLUSION_CULLING_ENABLED) {
            return frustum.containsAABB(aabb)
        }
        val chunkPositions: MutableSet<Vec2i> = HashSet()
        val sectionIndices = IntOpenHashSet()
        for (position in aabb.blockPositions) {
            chunkPositions += position.chunkPosition
            sectionIndices += position.sectionHeight - minSection
        }
        var visible = false
        chunkPositions@ for (chunkPosition in chunkPositions) {
            val visibility = getChunkVisibility(chunkPosition) ?: continue
            for (index in sectionIndices.intIterator()) {
                if (index < 0 || index > maxIndex) {
                    visible = true // ToDo: Not 100% correct, image looking from > maxIndex to < 0
                    break@chunkPositions
                }
                if (visibility[index]) {
                    visible = true
                    break@chunkPositions
                }
            }
        }

        if (!visible) {
            return false
        }

        return frustum.containsAABB(aabb)
    }

    fun isSectionVisible(chunkPosition: Vec2i, sectionHeight: Int, minPosition: Vec3i = DEFAULT_MIN_POSITION, maxPosition: Vec3i = ProtocolDefinition.CHUNK_SECTION_SIZE, checkChunk: Boolean = true): Boolean {
        if (checkChunk && !isChunkVisible(chunkPosition)) {
            return false
        }
        if (chunkPosition == cameraChunkPosition && sectionHeight == cameraSectionHeight) { // ToDo: Remove duplicated chunk position check
            return true
        }
        if (RenderConstants.OCCLUSION_CULLING_ENABLED) {
            if (getChunkVisibility(chunkPosition)?.getOrNull(sectionHeight - minSection) == false) {
                return false
            }
        }

        if (!frustum.containsChunk(chunkPosition, sectionHeight, minPosition, maxPosition)) {
            return false
        }
        return true
    }

    fun updateCamera(chunkPosition: Vec2i, sectionHeight: Int) {
        if (this.cameraChunkPosition == chunkPosition && this.cameraSectionHeight == sectionHeight) {
            return
        }
        this.cameraChunkPosition = chunkPosition
        this.cameraSectionHeight = sectionHeight
        this.minSection = connection.world.dimension?.minSection ?: 0
        this.maxSection = connection.world.dimension?.maxSection ?: 16
        this.sections = maxSection - minSection
        this.maxIndex = sections - 1
        calculateGraph()
    }


    private fun Array<Array<BooleanArray?>?>.getVisibility(chunkPosition: Vec2i): BooleanArray {
        val x = chunkPosition.x - chunkMin.x
        val y = chunkPosition.y - chunkMin.y

        var array = this[x]
        if (array == null) {
            array = arrayOfNulls(worldSize.y)
            this[x] = array
        }
        var innerArray = array[y]
        if (innerArray == null) {
            innerArray = BooleanArray(sections)
            array[y] = innerArray
        }
        return innerArray
    }

    private fun isInFrustum(chunkPosition: Vec2i, sectionHeight: Int): Boolean {
        val x = chunkPosition.x - chunkMin.x

        if (x >= frustumCache.size || x < 0) {
            return frustum.containsChunk(chunkPosition, sectionHeight)
        }
        var array = frustumCache[x]
        if (array == null) {
            array = ByteArray(worldSize.y)
            frustumCache[x] = array
        }
        val y = chunkPosition.y - chunkMin.y
        if (y >= array.size || y < 0) {
            return frustum.containsChunk(chunkPosition, sectionHeight)
        }
        var visibility = array[y]
        if (visibility == 0.toByte()) {
            visibility = if (frustum.containsChunk(chunkPosition, sectionHeight)) 1 else 0
            array[y] = visibility
        }
        return visibility == 1.toByte()
    }

    private fun checkSection(graph: Array<Array<BooleanArray?>?>, chunkPosition: Vec2i, sectionIndex: Int, chunk: Chunk, visibilities: BooleanArray, direction: Directions, directionX: Int, directionY: Int, directionZ: Int, ignoreVisibility: Boolean) {
        if ((direction == Directions.UP && sectionIndex >= maxIndex) || (direction == Directions.DOWN && sectionIndex < 0)) {
            return
        }
        if (!isInViewDistance(chunkPosition)) {
            return
        }
        val inverted = direction.inverted

        if (ignoreVisibility) {
            visibilities[sectionIndex] = true
        } else if (!isInFrustum(chunkPosition, sectionIndex + minSection)) {
            return
        }

        if (directionX <= 0 && (chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(inverted, Directions.WEST) != true) && chunkPosition.x > chunkMin.x) {
            val nextPosition = chunkPosition + Directions.WEST
            val nextChunk = chunk.neighbours?.get(1)
            if (nextChunk != null) {
                val nextVisibilities = graph.getVisibility(nextPosition)
                if (!nextVisibilities[sectionIndex]) {
                    nextVisibilities[sectionIndex] = true
                    checkSection(graph, nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.WEST, -1, directionY, directionZ, false)
                }
            }
        }

        if (directionX >= 0 && (chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(inverted, Directions.EAST) != true) && chunkPosition.x < chunkMax.x) {
            val nextPosition = chunkPosition + Directions.EAST
            val nextChunk = chunk.neighbours?.get(6)
            if (nextChunk != null) {
                val nextVisibilities = graph.getVisibility(nextPosition)
                if (!nextVisibilities[sectionIndex]) {
                    nextVisibilities[sectionIndex] = true
                    checkSection(graph, nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.EAST, 1, directionY, directionZ, false)
                }
            }
        }

        if (sectionIndex > 0 && directionY <= 0 && (chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(inverted, Directions.DOWN) != true)) {
            if (!visibilities[sectionIndex - 1]) {
                visibilities[sectionIndex - 1] = true
                checkSection(graph, chunkPosition, sectionIndex - 1, chunk, visibilities, Directions.DOWN, directionX, -1, directionZ, false)
            }
        }
        if (sectionIndex < maxIndex && directionY >= 0 && (chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(inverted, Directions.UP) != true)) {
            if (!visibilities[sectionIndex + 1]) {
                visibilities[sectionIndex + 1] = true
                checkSection(graph, chunkPosition, sectionIndex + 1, chunk, visibilities, Directions.UP, directionX, 1, directionZ, false)
            }
        }

        if (directionZ <= 0 && (chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(inverted, Directions.NORTH) != true) && chunkPosition.y > chunkMin.y) {
            val nextPosition = chunkPosition + Directions.NORTH
            val nextChunk = chunk.neighbours?.get(3)
            if (nextChunk != null) {
                val nextVisibilities = graph.getVisibility(nextPosition)
                if (!nextVisibilities[sectionIndex]) {
                    nextVisibilities[sectionIndex] = true
                    checkSection(graph, nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.NORTH, directionX, directionY, -1, false)
                }
            }
        }

        if (directionZ >= 0 && (chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(inverted, Directions.SOUTH) != true) && chunkPosition.y < chunkMax.y) {
            val nextPosition = chunkPosition + Directions.SOUTH
            val nextChunk = chunk.neighbours?.get(4)
            if (nextChunk != null) {
                val nextVisibilities = graph.getVisibility(nextPosition)
                if (!nextVisibilities[sectionIndex]) {
                    nextVisibilities[sectionIndex] = true
                    checkSection(graph, nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.SOUTH, directionX, directionY, 1, false)
                }
            }
        }
    }

    @Synchronized
    private fun calculateGraph() {
        if (!RenderConstants.OCCLUSION_CULLING_ENABLED) {
            return
        }
        connection.world.chunks.lock.acquire()
        recalculateNextFrame = false
        this.lastFrustumRevision = frustum.revision

        val chunkPosition = cameraChunkPosition
        val sectionHeight = cameraSectionHeight
        val cameraSectionIndex = (sectionHeight - minSection).clamp(0, maxIndex)
        this.viewDistance = connection.world.view.viewDistance

        val chunk = chunks[chunkPosition]
        if (chunk == null) {
            connection.world.chunks.lock.release()
            return
        }
        val worldSize = Vec2i(connection.world.chunkSize)
        worldSize += 3 // add 3 for forced neighbours and the camera chunk
        val chunkMin = chunkPosition - (worldSize / 2)
        chunkMin.x -= 1 // remove 1 for proper index calculation

        if (this.chunkMin != chunkMin || this.worldSize != worldSize) {
            this.chunkMin = chunkMin
            this.chunkMax = chunkMin + worldSize - 1
            this.worldSize = worldSize
            this.frustumCache = arrayOfNulls(worldSize.x)
        }

        val graph: Array<Array<BooleanArray?>?> = arrayOfNulls(worldSize.x)
        graph.getVisibility(chunkPosition)[cameraSectionIndex] = true

        for (direction in Directions.VALUES) {
            val nextPosition = chunkPosition + direction
            val nextChunk = connection.world[nextPosition] ?: continue
            val nextVisibility = graph.getVisibility(nextPosition)
            val vector = direction.vector
            checkSection(graph, nextPosition, cameraSectionIndex + vector.y, nextChunk, nextVisibility, direction, vector.x, vector.y, vector.z, true)
        }

        this.graph = graph


        connection.world.chunks.lock.release()

        connection.fireEvent(VisibilityGraphChangeEvent(renderWindow))
    }

    override fun onOcclusionChange() {
        recalculateNextFrame = true
    }

    fun draw() {
        if (recalculateNextFrame || frustum.revision != lastFrustumRevision) {
            calculateGraph()
        }
    }

    companion object {
        private val DEFAULT_MIN_POSITION = Vec3i.EMPTY
    }
}
