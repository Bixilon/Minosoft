/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.view

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.array.ArrayUtil.isIndex
import de.bixilon.kutil.array.BooleanArrayUtil.trySet
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateEvent
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkCreateUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkUnloadUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.NeighbourChangeUpdate
import de.bixilon.minosoft.data.world.positions.InChunkSectionPosition
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.sectionHeight
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
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
    private val context: RenderContext,
    camera: Camera,
) {
    private val connection = context.connection
    private val frustum = camera.matrixHandler.frustum
    private var cameraChunkPosition = Vec2i.EMPTY
    private var cameraSectionHeight = 0
    private var viewDistance = connection.world.view.viewDistance
    private val chunks = connection.world.chunks.chunks.unsafe
    private var lastFrustumRevision = -1


    private var invalid = false

    private var minSection = 0
    private var maxSection = 16
    private var maxIndex = 15
    private var sections = 16

    private var chunkMin = Vec2i.EMPTY
    private var chunkMax = Vec2i.EMPTY
    private var worldSize = Vec2i.EMPTY

    private var graph: VisibilityGraph = arrayOfNulls(0)

    private lateinit var frustumCache: Array<ByteArray?>

    // check for view distance (hide chunks that are far away)
    // check if direction is non-negative (i.e. basic frustum culling)
    // always show current chunk section
    // check occlusion culling
    // real frustum culling


    // occlusion culling
    // always show current section

    init {
        connection.world::occlusion.observe(this) { invalid = true }

        connection.events.listen<WorldUpdateEvent> {
            if (it.update !is ChunkCreateUpdate && it.update !is NeighbourChangeUpdate && it.update !is ChunkUnloadUpdate) {
                return@listen
            }
            invalid = true
        }
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
        for (position in aabb.positions()) {
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
                if (visibility[index + 1]) {
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

    fun isSectionVisible(chunkPosition: Vec2i, sectionHeight: Int, minPosition: InChunkSectionPosition = DEFAULT_MIN_POSITION, maxPosition: InChunkSectionPosition = ProtocolDefinition.CHUNK_SECTION_SIZE, checkChunk: Boolean = true): Boolean {
        if (checkChunk && !isChunkVisible(chunkPosition)) {
            return false
        }
        if (chunkPosition == cameraChunkPosition && sectionHeight == cameraSectionHeight) { // ToDo: Remove duplicated chunk position check
            return true
        }
        if (RenderConstants.OCCLUSION_CULLING_ENABLED) {
            if (getChunkVisibility(chunkPosition)?.getOrNull(sectionHeight - minSection + 1) == false) {
                return false
            }
        }

        if (!frustum.containsChunkSection(chunkPosition, sectionHeight, minPosition, maxPosition)) {
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
        this.minSection = connection.world.dimension.minSection
        this.maxSection = connection.world.dimension.maxSection
        this.sections = maxSection - minSection
        this.maxIndex = sections - 1
        calculateGraph()
    }


    private fun VisibilityGraph.getVisibility(chunkPosition: Vec2i): BooleanArray? {
        val x = chunkPosition.x - chunkMin.x
        val y = chunkPosition.y - chunkMin.y

        if (!this.isIndex(x)) return null
        var array = this[x]
        if (array == null) {
            array = arrayOfNulls(worldSize.y)
            this[x] = array
        }
        if (!array.isIndex(y)) return null

        var innerArray = array[y]
        if (innerArray == null) {
            innerArray = BooleanArray(sections + 2) // below and above dimension height
            array[y] = innerArray
        }
        return innerArray
    }

    private fun isInFrustum(chunkPosition: Vec2i, sectionHeight: Int): Boolean {
        val x = chunkPosition.x - chunkMin.x

        if (x >= frustumCache.size || x < 0) {
            return frustum.containsChunkSection(chunkPosition, sectionHeight)
        }
        var array = frustumCache[x]
        if (array == null) {
            array = ByteArray(worldSize.y)
            frustumCache[x] = array
        }
        val y = chunkPosition.y - chunkMin.y
        if (y >= array.size || y < 0) {
            return frustum.containsChunkSection(chunkPosition, sectionHeight)
        }
        var visibility = array[y]
        if (visibility == 0.toByte()) {
            visibility = if (frustum.containsChunk(chunkPosition)) 1 else 2
            array[y] = visibility
        }
        if (visibility == 2.toByte()) {
            return false
        }
        return frustum.containsChunkSection(chunkPosition, sectionHeight)
    }

    private fun VisibilityGraph.checkSection(chunkPosition: Vec2i, sectionIndex: Int, chunk: Chunk, visibilities: BooleanArray, direction: Directions, directionX: Int, directionY: Int, directionZ: Int, ignoreVisibility: Boolean) {
        if ((direction == Directions.UP && sectionIndex >= maxIndex) || (direction == Directions.DOWN && sectionIndex < 0)) {
            return
        }
        if (!isInViewDistance(chunkPosition)) {
            return
        }
        val inverted = direction.inverted
        val visibilitySectionIndex = sectionIndex + 1

        if (ignoreVisibility) {
            visibilities[visibilitySectionIndex] = true
        } else if (!isInFrustum(chunkPosition, sectionIndex + minSection)) {
            return
        }

        val section = chunk.sections.getOrNull(sectionIndex)?.blocks

        val nextPosition = Vec2i()

        if (directionX <= 0 && (section?.occlusion?.isOccluded(inverted, Directions.WEST) != true) && chunkPosition.x > chunkMin.x) {
            nextPosition.x = chunkPosition.x - 1; nextPosition.y = chunkPosition.y // + WEST
            val nextChunk = chunk.neighbours[ChunkNeighbours.WEST]
            if (nextChunk != null) {
                val nextVisibilities = getVisibility(nextPosition) ?: return
                if (!nextVisibilities[visibilitySectionIndex]) {
                    nextVisibilities[visibilitySectionIndex] = true
                    checkSection(nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.WEST, -1, directionY, directionZ, false)
                }
            }
        }

        if (directionX >= 0 && (section?.occlusion?.isOccluded(inverted, Directions.EAST) != true) && chunkPosition.x < chunkMax.x) {
            nextPosition.x = chunkPosition.x + 1; nextPosition.y = chunkPosition.y // + EAST
            val nextChunk = chunk.neighbours[ChunkNeighbours.EAST]
            if (nextChunk != null) {
                val nextVisibilities = getVisibility(nextPosition) ?: return
                if (!nextVisibilities[visibilitySectionIndex]) {
                    nextVisibilities[visibilitySectionIndex] = true
                    checkSection(nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.EAST, 1, directionY, directionZ, false)
                }
            }
        }

        if (sectionIndex > 0 && directionY <= 0 && (section?.occlusion?.isOccluded(inverted, Directions.DOWN) != true)) {
            if (!visibilities[visibilitySectionIndex - 1]) {
                visibilities[visibilitySectionIndex - 1] = true
                checkSection(chunkPosition, sectionIndex - 1, chunk, visibilities, Directions.DOWN, directionX, -1, directionZ, false)
            }
        }
        if (sectionIndex < maxIndex && directionY >= 0 && (section?.occlusion?.isOccluded(inverted, Directions.UP) != true)) {
            if (!visibilities[visibilitySectionIndex + 1]) {
                visibilities[visibilitySectionIndex + 1] = true
                checkSection(chunkPosition, sectionIndex + 1, chunk, visibilities, Directions.UP, directionX, 1, directionZ, false)
            }
        }

        if (directionZ <= 0 && (section?.occlusion?.isOccluded(inverted, Directions.NORTH) != true) && chunkPosition.y > chunkMin.y) {
            nextPosition.x = chunkPosition.x; nextPosition.y = chunkPosition.y - 1 // + NORTH
            val nextChunk = chunk.neighbours[ChunkNeighbours.NORTH]
            if (nextChunk != null) {
                val nextVisibilities = getVisibility(nextPosition) ?: return
                if (!nextVisibilities[visibilitySectionIndex]) {
                    nextVisibilities[visibilitySectionIndex] = true
                    checkSection(nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.NORTH, directionX, directionY, -1, false)
                }
            }
        }

        if (directionZ >= 0 && (section?.occlusion?.isOccluded(inverted, Directions.SOUTH) != true) && chunkPosition.y < chunkMax.y) {
            nextPosition.x = chunkPosition.x; nextPosition.y = chunkPosition.y + 1 // + SOUTH
            val nextChunk = chunk.neighbours[ChunkNeighbours.SOUTH]
            if (nextChunk != null) {
                val nextVisibilities = getVisibility(nextPosition) ?: return
                if (!nextVisibilities[visibilitySectionIndex]) {
                    nextVisibilities[visibilitySectionIndex] = true
                    checkSection(nextPosition, sectionIndex, nextChunk, nextVisibilities, Directions.SOUTH, directionX, directionY, 1, false)
                }
            }
        }
    }


    private fun VisibilityGraph.startCheck(direction: Directions, chunkPosition: Vec2i, cameraSectionIndex: Int) {
        val nextPosition = chunkPosition + direction
        val nextChunk = connection.world.chunks[nextPosition] ?: return
        val nextVisibility = getVisibility(nextPosition)
        val vector = direction.vector
        checkSection(nextPosition, cameraSectionIndex + vector.y, nextChunk, nextVisibility ?: return, direction, vector.x, vector.y, vector.z, true)
    }

    @Synchronized
    private fun calculateGraph() {
        if (!RenderConstants.OCCLUSION_CULLING_ENABLED) {
            connection.events.fire(VisibilityGraphChangeEvent(context))
            return
        }
        connection.world.lock.acquire()
        invalid = false
        this.lastFrustumRevision = frustum.revision

        val chunkPosition = cameraChunkPosition
        val sectionHeight = cameraSectionHeight
        val cameraSectionIndex = (sectionHeight - minSection).clamp(-1, maxIndex + 1)  // clamp 1 section below or above
        this.viewDistance = connection.world.view.viewDistance

        val chunk = chunks[chunkPosition]
        if (chunk == null) {
            connection.world.lock.release()
            return
        }
        val worldSize = Vec2i(connection.world.chunks.size.size.size)
        worldSize += 3 // add 3 for forced neighbours and the camera chunk
        val chunkMin = chunkPosition - (worldSize / 2)
        chunkMin.x -= 1 // remove 1 for proper index calculation

        if (this.chunkMin != chunkMin || this.worldSize != worldSize) {
            this.chunkMin = chunkMin
            this.chunkMax = chunkMin + worldSize - 1
            this.worldSize = worldSize
        }
        this.frustumCache = arrayOfNulls(worldSize.x)

        val graph: VisibilityGraph = arrayOfNulls(worldSize.x)
        graph.getVisibility(chunkPosition)?.trySet(cameraSectionIndex + 1, true)


        for (direction in Directions.VALUES) {
            graph.startCheck(direction, chunkPosition, cameraSectionIndex)
        }

        this.graph = graph


        connection.world.lock.release()

        connection.events.fire(VisibilityGraphChangeEvent(context))
    }

    fun draw() {
        if (invalid || frustum.revision != lastFrustumRevision) {
            calculateGraph()
        }
    }

    companion object {
        private val DEFAULT_MIN_POSITION = Vec3i.EMPTY
    }
}
