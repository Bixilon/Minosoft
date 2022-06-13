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
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.OcclusionUpdateCallback
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
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
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
    private val chunks = connection.world.chunks.original
    private var lastFrustumRevision = -1


    private var recalculateNextFrame = false

    private var minSection = 0
    private var maxSection = 16
    private var maxIndex = 15
    private var sections = 16

    private var chunkMin = Vec2i.EMPTY
    private var worldSize = Vec2i.EMPTY

    private var graph: Array<Array<BooleanArray?>?> = arrayOfNulls(0)

    private lateinit var chunkCache: Array<Array<Chunk?>?>
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

        if (!frustum.containsChunk(chunkPosition, sectionHeight, minPosition, maxPosition)) {
            return false
        }
        if (!RenderConstants.OCCLUSION_CULLING_ENABLED) {
            return true
        }
        val visible = getChunkVisibility(chunkPosition)?.getOrNull(sectionHeight - minSection)

        return visible == true
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

    private fun getChunk(chunkPosition: Vec2i): Chunk? {
        val x = chunkPosition.x - chunkMin.x

        if (x >= chunkCache.size || x < 0) {
            return null
        }
        var array = chunkCache[x]
        if (array == null) {
            array = arrayOfNulls(worldSize.y)
            chunkCache[x] = array
        }
        val y = chunkPosition.y - chunkMin.y
        if (y >= array.size || y < 0) {
            return null
        }
        var chunk = array[y]
        if (chunk == null) {
            chunk = chunks[chunkPosition]
            array[y] = chunk
        }
        return chunk
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

    private fun checkSection(graph: Array<Array<BooleanArray?>?>, chunkPosition: Vec2i, sectionIndex: Int, chunk: Chunk, visibilities: BooleanArray, direction: Directions, directionVector: Vec3i, steps: Int, ignoreVisibility: Boolean) {
        if (steps > 64 * 3) {
            Log.log(LogMessageType.OTHER, LogLevels.WARN) { "Potential stack overflow: $chunkPosition:$sectionIndex $direction $directionVector" }
            return
        }
        if ((direction == Directions.UP && sectionIndex >= maxIndex) || (direction == Directions.DOWN && sectionIndex < 0)) {
            return
        }
        if (!isInViewDistance(chunkPosition)) {
            return
        }
        val inverted = direction.inverted
        val nextStep = steps + 1

        if (ignoreVisibility) {
            visibilities[sectionIndex] = true
        } else if (!isInFrustum(chunkPosition, sectionIndex + minSection)) {
            return
        }

        if (directionVector.x <= 0 && (chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(inverted, Directions.WEST) != true)) {
            val nextPosition = chunkPosition + Directions.WEST
            val nextChunk = getChunk(nextPosition)
            if (nextChunk != null) {
                val nextVisibilities = graph.getVisibility(nextPosition)
                if (!nextVisibilities[sectionIndex]) {
                    nextVisibilities[sectionIndex] = true
                    checkSection(graph, nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.WEST, directionVector.modify(0, -1), nextStep, false)
                }
            }
        }

        if (directionVector.x >= 0 && (chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(inverted, Directions.EAST) != true)) {
            val nextPosition = chunkPosition + Directions.EAST
            val nextChunk = getChunk(nextPosition)
            if (nextChunk != null) {
                val nextVisibilities = graph.getVisibility(nextPosition)
                if (!nextVisibilities[sectionIndex]) {
                    nextVisibilities[sectionIndex] = true
                    checkSection(graph, nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.EAST, directionVector.modify(0, 1), nextStep, false)
                }
            }
        }

        if (sectionIndex > 0 && directionVector.y <= 0 && (chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(inverted, Directions.DOWN) != true)) {
            if (!visibilities[sectionIndex - 1]) {
                visibilities[sectionIndex - 1] = true
                checkSection(graph, chunkPosition, sectionIndex - 1, chunk, visibilities, Directions.DOWN, directionVector.modify(1, -1), nextStep, false)
            }
        }
        if (sectionIndex < maxIndex && directionVector.y >= 0 && (chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(inverted, Directions.UP) != true)) {
            if (!visibilities[sectionIndex + 1]) {
                visibilities[sectionIndex + 1] = true
                checkSection(graph, chunkPosition, sectionIndex + 1, chunk, visibilities, Directions.UP, directionVector.modify(1, 1), nextStep, false)
            }
        }

        if (directionVector.z <= 0 && (chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(inverted, Directions.NORTH) != true)) {
            val nextPosition = chunkPosition + Directions.NORTH
            val nextChunk = getChunk(nextPosition)
            if (nextChunk != null) {
                val nextVisibilities = graph.getVisibility(nextPosition)
                if (!nextVisibilities[sectionIndex]) {
                    nextVisibilities[sectionIndex] = true
                    checkSection(graph, nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.NORTH, directionVector.modify(2, -1), nextStep, false)
                }
            }
        }

        if (directionVector.z >= 0 && (chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(inverted, Directions.SOUTH) != true)) {
            val nextPosition = chunkPosition + Directions.SOUTH
            val nextChunk = getChunk(nextPosition)
            if (nextChunk != null) {
                val nextVisibilities = graph.getVisibility(nextPosition)
                if (!nextVisibilities[sectionIndex]) {
                    nextVisibilities[sectionIndex] = true
                    checkSection(graph, nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.SOUTH, directionVector.modify(2, 1), nextStep, false)
                }
            }
        }
    }

    private fun Vec3i.modify(axis: Int, value: Int): Vec3i {
        val array = this.array
        if (array[axis] == value) {
            return this
        }
        val ret = Vec3i(this)
        ret[axis] = value
        return ret
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
        val worldSize = connection.world.chunkSize
        worldSize += 3 // add 3 for forced neighbours and the camera chunk
        val chunkMin = chunkPosition - (worldSize / 2)
        chunkMin.x -= 1 // remove 1 for proper index calculation

        if (this.chunkMin != chunkMin || this.worldSize != worldSize) {
            this.chunkMin = chunkMin
            this.worldSize = worldSize
            this.chunkCache = arrayOfNulls(worldSize.x)
            this.frustumCache = arrayOfNulls(worldSize.x)
        }

        val graph: Array<Array<BooleanArray?>?> = arrayOfNulls(worldSize.x)
        graph.getVisibility(chunkPosition)[cameraSectionIndex] = true

        for (direction in Directions.VALUES) {
            val nextPosition = chunkPosition + direction
            val nextChunk = getChunk(nextPosition) ?: continue
            val nextVisibility = graph.getVisibility(nextPosition)
            checkSection(graph, nextPosition, cameraSectionIndex + direction.vector.y, nextChunk, nextVisibility, direction, direction.vector, 0, true)
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
