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

package de.bixilon.minosoft.gui.rendering.camera.visibility

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.array.ArrayUtil.isIndex
import de.bixilon.kutil.array.BooleanArrayUtil.trySet
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.direction.DirectionVector
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateEvent
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkCreateUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkUnloadUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.NeighbourChangeUpdate
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.camera.frustum.Frustum
import de.bixilon.minosoft.gui.rendering.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.ChunkUtil.isInViewDistance
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
    private val session = context.session
    private val frustum = camera.matrixHandler.frustum
    private var cameraChunkPosition = ChunkPosition.EMPTY
    private var cameraSectionHeight = 0
    private var viewDistance = session.world.view.viewDistance
    private val chunks = session.world.chunks.chunks.unsafe
    private var lastFrustumRevision = -1


    private var invalid = false

    private var minSection = 0
    private var maxSection = 16
    private var maxIndex = 15
    private var sections = 16

    private var chunkMin = ChunkPosition.EMPTY
    private var chunkMax = ChunkPosition.EMPTY
    private var worldSize = Vec2i()

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
        session.world::occlusion.observe(this) { invalid = true }

        session.events.listen<WorldUpdateEvent> {
            if (it.update !is ChunkCreateUpdate && it.update !is NeighbourChangeUpdate && it.update !is ChunkUnloadUpdate) {
                return@listen
            }
            invalid = true
        }
    }

    fun isInViewDistance(chunkPosition: ChunkPosition): Boolean {
        return chunkPosition.isInViewDistance(session.world.view.viewDistance, cameraChunkPosition)
    }

    fun isChunkVisible(chunkPosition: ChunkPosition): Boolean {
        if (!isInViewDistance(chunkPosition)) {
            return false
        }
        if (!RenderConstants.OCCLUSION_CULLING_ENABLED) {
            return true
        }

        // ToDo: basic frustum culling
        return getChunkVisibility(chunkPosition) != null // ToDo: check if all values are false
    }

    private fun getChunkVisibility(chunkPosition: ChunkPosition): BooleanArray? {
        val x = chunkPosition.x - chunkMin.x
        val y = chunkPosition.z - chunkMin.z

        return this.graph.getOrNull(x)?.getOrNull(y)
    }

    fun isAABBOccluded(aabb: AABB): Boolean {
        if (!RenderConstants.OCCLUSION_CULLING_ENABLED) {
            return frustum.containsAABB(aabb)
        }
        val chunkPositions: MutableSet<ChunkPosition> = HashSet(4, 1.0f)
        val sectionIndices = IntOpenHashSet()
        for (position in aabb.positions()) { // TODO: use ChunkPosition iterator
            chunkPositions += position.chunkPosition
            sectionIndices += position.sectionHeight - minSection
        }

        for (chunkPosition in chunkPositions) {
            val visibility = getChunkVisibility(chunkPosition) ?: continue
            for (index in sectionIndices.intIterator()) {
                if (index < 0 || index > maxIndex) {
                    // ToDo: Not 100% correct, image looking from > maxIndex to < 0
                    return false
                }
                if (visibility[index + 1]) {
                    return false
                }
            }
        }

        return true
    }

    fun isAABBVisible(aabb: AABB): Boolean {
        if (isAABBOccluded(aabb)) return false

        return frustum.containsAABB(aabb)
    }

    fun isSectionVisible(chunkPosition: ChunkPosition, sectionHeight: Int, minPosition: InSectionPosition = Frustum.SECTION_MIN_POSITION, maxPosition: InSectionPosition = Frustum.SECTION_MIN_POSITION, checkChunk: Boolean = true): Boolean {
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

    fun updateCamera(chunkPosition: ChunkPosition, sectionHeight: Int) {
        if (this.cameraChunkPosition == chunkPosition && this.cameraSectionHeight == sectionHeight) {
            return
        }
        this.cameraChunkPosition = chunkPosition
        this.cameraSectionHeight = sectionHeight
        this.minSection = session.world.dimension.minSection
        this.maxSection = session.world.dimension.maxSection
        this.sections = maxSection - minSection
        this.maxIndex = sections - 1
        calculateGraph()
    }


    private fun VisibilityGraph.getVisibility(chunkPosition: ChunkPosition): BooleanArray? {
        val x = chunkPosition.x - chunkMin.x
        val y = chunkPosition.z - chunkMin.z

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

    private fun isInFrustum(chunkPosition: ChunkPosition, sectionHeight: Int): Boolean {
        val x = chunkPosition.x - chunkMin.x

        if (x >= frustumCache.size || x < 0) {
            return frustum.containsChunkSection(chunkPosition, sectionHeight)
        }
        var array = frustumCache[x]
        if (array == null) {
            array = ByteArray(worldSize.y)
            frustumCache[x] = array
        }
        val z = chunkPosition.z - chunkMin.z
        if (z >= array.size || z < 0) {
            return frustum.containsChunkSection(chunkPosition, sectionHeight)
        }
        var visibility = array[z]
        if (visibility == 0.toByte()) {
            visibility = if (frustum.containsChunk(chunkPosition)) 1 else 2
            array[z] = visibility
        }
        if (visibility == 2.toByte()) {
            return false
        }
        return frustum.containsChunkSection(chunkPosition, sectionHeight)
    }

    private fun VisibilityGraph.checkSection(chunkPosition: ChunkPosition, sectionIndex: Int, chunk: Chunk, visibilities: BooleanArray, direction: Directions, vector: DirectionVector, ignoreVisibility: Boolean) {
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


        if (vector.x <= 0 && (section?.occlusion?.isOccluded(inverted, Directions.WEST) != true) && chunkPosition.x > chunkMin.x) {
            val nextChunk = chunk.neighbours[Directions.WEST]
            if (nextChunk != null) {
                val next = chunkPosition.minusX()
                val nextVisibilities = getVisibility(next) ?: return
                if (!nextVisibilities[visibilitySectionIndex]) {
                    nextVisibilities[visibilitySectionIndex] = true
                    checkSection(next, sectionIndex, nextChunk, nextVisibilities, Directions.WEST, vector.with(Directions.WEST), false)
                }
            }
        }

        if (vector.x >= 0 && (section?.occlusion?.isOccluded(inverted, Directions.EAST) != true) && chunkPosition.x < chunkMax.x) {
            val nextChunk = chunk.neighbours[Directions.EAST]
            if (nextChunk != null) {
                val next = chunkPosition.plusX()
                val nextVisibilities = getVisibility(next) ?: return
                if (!nextVisibilities[visibilitySectionIndex]) {
                    nextVisibilities[visibilitySectionIndex] = true
                    checkSection(next, sectionIndex, nextChunk, nextVisibilities, Directions.EAST, vector.with(Directions.EAST), false)
                }
            }
        }

        if (sectionIndex > 0 && vector.y <= 0 && (section?.occlusion?.isOccluded(inverted, Directions.DOWN) != true)) {
            if (!visibilities[visibilitySectionIndex - 1]) {
                visibilities[visibilitySectionIndex - 1] = true
                checkSection(chunkPosition, sectionIndex - 1, chunk, visibilities, Directions.DOWN, vector.with(Directions.DOWN), false)
            }
        }
        if (sectionIndex < maxIndex && vector.y >= 0 && (section?.occlusion?.isOccluded(inverted, Directions.UP) != true)) {
            if (!visibilities[visibilitySectionIndex + 1]) {
                visibilities[visibilitySectionIndex + 1] = true
                checkSection(chunkPosition, sectionIndex + 1, chunk, visibilities, Directions.UP, vector.with(Directions.UP), false)
            }
        }

        if (vector.z <= 0 && (section?.occlusion?.isOccluded(inverted, Directions.NORTH) != true) && chunkPosition.z > chunkMin.z) {
            val nextChunk = chunk.neighbours[Directions.NORTH]
            if (nextChunk != null) {
                val next = chunkPosition.minusZ()
                val nextVisibilities = getVisibility(next) ?: return
                if (!nextVisibilities[visibilitySectionIndex]) {
                    nextVisibilities[visibilitySectionIndex] = true
                    checkSection(next, sectionIndex, nextChunk, nextVisibilities, Directions.NORTH, vector.with(Directions.NORTH), false)
                }
            }
        }

        if (vector.z >= 0 && (section?.occlusion?.isOccluded(inverted, Directions.SOUTH) != true) && chunkPosition.z < chunkMax.z) {
            val nextChunk = chunk.neighbours[Directions.SOUTH]
            if (nextChunk != null) {
                val next = chunkPosition.plusZ()
                val nextVisibilities = getVisibility(next) ?: return
                if (!nextVisibilities[visibilitySectionIndex]) {
                    nextVisibilities[visibilitySectionIndex] = true
                    checkSection(next, sectionIndex, nextChunk, nextVisibilities, Directions.SOUTH, vector.with(Directions.SOUTH), false)
                }
            }
        }
    }


    private fun VisibilityGraph.startCheck(direction: Directions, chunkPosition: ChunkPosition, cameraSectionIndex: Int) {
        val next = chunkPosition + direction
        val nextChunk = session.world.chunks[next] ?: return
        val nextVisibility = getVisibility(next)
        checkSection(next, cameraSectionIndex + direction.vector.y, nextChunk, nextVisibility ?: return, direction, direction.vector, true)
    }

    @Synchronized
    private fun calculateGraph() {
        if (!RenderConstants.OCCLUSION_CULLING_ENABLED) {
            session.events.fire(VisibilityGraphChangeEvent(context))
            return
        }
        session.world.lock.acquire()
        invalid = false
        this.lastFrustumRevision = frustum.revision

        val chunkPosition = cameraChunkPosition
        val sectionHeight = cameraSectionHeight
        val cameraSectionIndex = (sectionHeight - minSection).clamp(-1, maxIndex + 1)  // clamp 1 section below or above
        this.viewDistance = session.world.view.viewDistance

        val chunk = chunks[chunkPosition]
        if (chunk == null) {
            session.world.lock.release()
            return
        }
        val worldSize = session.world.chunks.size.size.size
        worldSize += 3 // add 3 for forced neighbours and the camera chunk
        var chunkMin = chunkPosition - ChunkPosition(worldSize.x / 2, worldSize.y / 2)
        chunkMin = ChunkPosition(chunkMin.x - 1, chunkMin.z) // remove 1 for proper index calculation

        if (this.chunkMin != chunkMin || this.worldSize != worldSize) {
            this.chunkMin = chunkMin
            this.chunkMax = chunkMin + ChunkPosition(worldSize.x - 1, worldSize.y - 1)
            this.worldSize = worldSize
        }
        this.frustumCache = arrayOfNulls(worldSize.x)

        val graph: VisibilityGraph = arrayOfNulls(worldSize.x)
        graph.getVisibility(chunkPosition)?.trySet(cameraSectionIndex + 1, true)


        for (direction in Directions.VALUES) {
            graph.startCheck(direction, chunkPosition, cameraSectionIndex)
        }

        this.graph = graph


        session.world.lock.release()

        session.events.fire(VisibilityGraphChangeEvent(context))
    }

    fun draw() {
        if (invalid || frustum.revision != lastFrustumRevision) {
            calculateGraph()
        }
    }
}
