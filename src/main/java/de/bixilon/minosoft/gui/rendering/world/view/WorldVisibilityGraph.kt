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
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.OcclusionUpdateCallback
import de.bixilon.minosoft.data.world.container.BlockSectionDataProvider
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.modding.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
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
    camera: Camera,
) : OcclusionUpdateCallback {
    private val connection = renderWindow.connection
    private val frustum = camera.matrixHandler.frustum
    private var cameraChunkPosition = Vec2i.EMPTY
    private var cameraSectionHeight = 0
    private var viewDistance = connection.world.view.viewDistance
    private val chunks = connection.world.chunks.original


    private var recalculateNextFrame = false

    private var minSection = 0
    private var maxSection = 16
    private var maxIndex = 15
    private var sections = 16

    private var chunkMin = Vec2i.EMPTY
    private var worldSize = Vec2i.EMPTY

    private var visibilityLock = SimpleLock()
    private var visibilities: HashMap<Vec2i, BooleanArray> = HashMap()

    private lateinit var chunkCache: Array<Array<Chunk?>?>

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

        visibilityLock.acquire()
        val visible = visibilities[chunkPosition]
        visibilityLock.release()

        // ToDo: basic frustum culling
        return visible?.isNotEmpty() ?: false
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
        visibilityLock.acquire()
        chunkPositions@ for (chunkPosition in chunkPositions) {
            val visibility = this.visibilities[chunkPosition] ?: continue
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
        visibilityLock.release()

        if (!visible) {
            return false
        }

        return frustum.containsAABB(aabb)
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
        if (!RenderConstants.OCCLUSION_CULLING_ENABLED) {
            return true
        }
        visibilityLock.acquire()
        val visible = this.visibilities[chunkPosition]?.get(sectionHeight - minSection)
        visibilityLock.release()

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


    private fun Array<Array<Array<BooleanArray>?>?>.getVisibility(chunkPosition: Vec2i): Array<BooleanArray> {
        val x = chunkPosition.x - chunkMin.x
        val y = chunkPosition.y - chunkMin.y

        var array = this[x]
        if (array == null) {
            array = arrayOfNulls(worldSize.y)
            this[x] = array
        }
        var innerArray = array[y]
        if (innerArray == null) {
            innerArray = Array(sections) { BooleanArray(BlockSectionDataProvider.CUBE_DIRECTION_COMBINATIONS) }
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
        if (y >= chunkCache.size || y < 0) {
            return null
        }
        var chunk = array[y]
        if (chunk == null) {
            chunk = chunks[chunkPosition]
            array[y] = chunk
        }
        return chunk
    }

    private fun checkSection(graph: Array<Array<Array<BooleanArray>?>?>, chunkPosition: Vec2i, sectionIndex: Int, chunk: Chunk, visibilities: Array<BooleanArray>, direction: Directions, directionVector: Vec3i, steps: Int, ignoreVisibility: Boolean) {
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
            visibilities[sectionIndex] = TRUE_ARRAY
        }

        val downIndex = BlockSectionDataProvider.getIndex(inverted, Directions.DOWN)
        if (sectionIndex > 0 && directionVector.y <= 0 && (ignoreVisibility || chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(downIndex) != true)) {
            val visibility = visibilities[sectionIndex - 1]
            if (!visibility[downIndex]) {
                visibility[downIndex] = true
                checkSection(graph, chunkPosition, sectionIndex - 1, chunk, visibilities, Directions.DOWN, directionVector.modify(1, -1), nextStep, false)
            }
        }
        val upIndex = BlockSectionDataProvider.getIndex(inverted, Directions.UP)
        if (sectionIndex < maxIndex && directionVector.y >= 0 && (ignoreVisibility || chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(upIndex) != true)) {
            val visibility = visibilities[sectionIndex + 1]
            if (!visibility[upIndex]) {
                visibility[upIndex] = true
                checkSection(graph, chunkPosition, sectionIndex + 1, chunk, visibilities, Directions.UP, directionVector.modify(1, 1), nextStep, false)
            }
        }

        val northIndex = BlockSectionDataProvider.getIndex(inverted, Directions.NORTH)
        if (directionVector.z <= 0 && (ignoreVisibility || chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(northIndex) != true)) {
            val nextPosition = chunkPosition + Directions.NORTH
            val nextChunk = getChunk(nextPosition)
            if (nextChunk != null) {
                val nextVisibilities = graph.getVisibility(chunkPosition)
                val visibility = nextVisibilities[sectionIndex]
                if (!visibility[northIndex]) {
                    visibility[northIndex] = true
                    checkSection(graph, nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.NORTH, directionVector.modify(2, -1), nextStep, false)
                }
            }
        }
        val southIndex = BlockSectionDataProvider.getIndex(inverted, Directions.SOUTH)
        if (directionVector.z >= 0 && (ignoreVisibility || chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(southIndex) != true)) {
            val nextPosition = chunkPosition + Directions.SOUTH
            val nextChunk = getChunk(nextPosition)
            if (nextChunk != null) {
                val nextVisibilities = graph.getVisibility(chunkPosition)
                val visibility = nextVisibilities[sectionIndex]
                if (!visibility[southIndex]) {
                    visibility[southIndex] = true
                    checkSection(graph, nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.SOUTH, directionVector.modify(2, 1), nextStep, false)
                }
            }
        }

        val westIndex = BlockSectionDataProvider.getIndex(inverted, Directions.WEST)
        if (directionVector.x <= 0 && (ignoreVisibility || chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(westIndex) != true)) {
            val nextPosition = chunkPosition + Directions.WEST
            val nextChunk = getChunk(nextPosition)
            if (nextChunk != null) {
                val nextVisibilities = graph.getVisibility(chunkPosition)
                val visibility = nextVisibilities[sectionIndex]
                if (!visibility[westIndex]) {
                    visibility[westIndex] = true
                    checkSection(graph, nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.WEST, directionVector.modify(0, -1), nextStep, false)
                }
            }
        }

        val eastIndex = BlockSectionDataProvider.getIndex(inverted, Directions.EAST)
        if (directionVector.x >= 0 && (ignoreVisibility || chunk.sections?.get(sectionIndex)?.blocks?.isOccluded(eastIndex) != true)) {
            val nextPosition = chunkPosition + Directions.EAST
            val nextChunk = getChunk(nextPosition)
            if (nextChunk != null) {
                val nextVisibilities = graph.getVisibility(chunkPosition)
                val visibility = nextVisibilities[sectionIndex]
                if (!visibility[eastIndex]) {
                    visibility[eastIndex] = true
                    checkSection(graph, nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.EAST, directionVector.modify(0, 1), nextStep, false)
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

    private fun calculateGraph() {
        if (!RenderConstants.OCCLUSION_CULLING_ENABLED) {
            return
        }
        connection.world.chunks.lock.acquire()
        recalculateNextFrame = false
        val start = TimeUtil.nanos
        println("Calculating graph...")

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
        val chunkMin = cameraChunkPosition - (worldSize / 2)
        chunkMin.x -= 1 // remove 1 for proper index calculation

        if (this.chunkMin != chunkMin || this.worldSize != worldSize) {
            this.chunkMin = chunkMin
            this.worldSize = worldSize
            this.chunkCache = arrayOfNulls(worldSize.x)
        }

        val graph: Array<Array<Array<BooleanArray>?>?> = arrayOfNulls(worldSize.x)

        for (direction in Directions.VALUES) {
            val nextPosition = chunkPosition + direction
            val nextChunk = getChunk(nextPosition) ?: continue
            val nextVisibility = graph.getVisibility(chunkPosition)
            checkSection(graph, nextPosition, cameraSectionIndex, nextChunk, nextVisibility, direction, direction.vector, 0, true)
        }

        updateVisibilityGraph(graph)


        val end = TimeUtil.nanos
        println("Done in ${(end - start) / 1000}")

        connection.world.chunks.lock.release()

        connection.fireEvent(VisibilityGraphChangeEvent(renderWindow))
    }

    private fun updateVisibilityGraph(graph: Array<Array<Array<BooleanArray>?>?>) {
        visibilityLock.lock()
        this.visibilities.clear()


        for ((chunkX, array) in graph.withIndex()) {
            if (array == null) {
                continue
            }

            for ((chunkY, sections) in array.withIndex()) {
                if (sections == null) {
                    continue
                }
                val visibility = BooleanArray(maxIndex + 1)
                var chunkVisible = false
                for ((sectionIndex, combinations) in sections.withIndex()) {
                    for (combination in combinations) {
                        if (!combination) {
                            continue
                        }

                        visibility[sectionIndex] = true
                        chunkVisible = true

                        break
                    }
                }
                if (chunkVisible) {
                    this.visibilities[Vec2i(chunkX + chunkMin.x, chunkY + chunkMin.y)] = visibility
                }
            }
        }

        visibilityLock.unlock()
    }

    override fun onOcclusionChange() {
        recalculateNextFrame = true
    }

    fun draw() {
        if (recalculateNextFrame) {
            calculateGraph()
        }
    }

    companion object {
        private val DEFAULT_MIN_POSITION = Vec3i.EMPTY
        private val DEFAULT_MAX_POSITION = Vec3i(ProtocolDefinition.SECTION_WIDTH_X, ProtocolDefinition.SECTION_HEIGHT_Y, ProtocolDefinition.SECTION_WIDTH_Z)
        private val TRUE_ARRAY = BooleanArray(BlockSectionDataProvider.CUBE_DIRECTION_COMBINATIONS) { true }
    }
}
