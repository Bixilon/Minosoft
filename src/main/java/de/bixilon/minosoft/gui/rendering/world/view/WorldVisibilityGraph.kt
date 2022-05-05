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
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.chunk.ChunkUtil.isInViewDistance
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
        calculateGraph()
    }

    private fun checkDirection(startPosition: Vec2i, sectionHeight: Int, initialDirection: Directions, direction: Directions = initialDirection, iteration: Int = 2) {
        var distance = 0
        var chunkPosition = startPosition
        while (true) {
            val chunk = connection.world[chunkPosition] ?: break
            val visibility = visibilities.getOrPut(chunkPosition) { IntOpenHashSet() }
            visibility += sectionHeight

            val section = chunk[sectionHeight]
            if (distance > viewDistance) {
                break
            }
            if (iteration > 0) {
                val rotated1 = direction.rotateYC()
                if (initialDirection != rotated1.inverted && (section == null || !section.blocks.isOccluded(direction, rotated1))) {
                    checkDirection(chunkPosition + rotated1, sectionHeight, initialDirection, rotated1, iteration - 1)
                }
                val rotated2 = direction.rotateYCC()
                if (initialDirection != rotated2.inverted && direction != rotated2 && (section == null || !section.blocks.isOccluded(direction, rotated2))) {
                    checkDirection(chunkPosition + rotated2, sectionHeight, initialDirection, rotated2, iteration - 1)
                }
            }

            if (section != null && section.blocks.isOccluded(direction.inverted, direction)) {
                break
            }
            chunkPosition += direction
            distance++
        }
    }

    private fun calculateGraph() {
        visibilityLock.lock()
        this.visibilities.clear()

        val chunkPosition = cameraChunkPosition
        val sectionHeight = cameraSectionHeight
        this.viewDistance = connection.world.view.viewDistance

        val chunk = connection.world.chunks[chunkPosition]
        if (chunk == null) {
            visibilityLock.unlock()
            return
        }
        val chunkVisibility = visibilities.getOrPut(chunkPosition) { IntOpenHashSet() }

        chunkVisibility += sectionHeight

        for (height in sectionHeight - 1 downTo (connection.world.dimension?.lowestSection ?: 0)) {
            val section = chunk[height] ?: continue
            chunkVisibility += height
            if (section.blocks.isOccluded(Directions.UP, Directions.DOWN)) {
                break
            }
        }
        for (height in sectionHeight + 1 downTo (connection.world.dimension?.highestSection ?: 16)) {
            val section = chunk[height] ?: continue
            chunkVisibility += height
            if (section.blocks.isOccluded(Directions.DOWN, Directions.UP)) {
                break
            }
        }
        checkDirection(chunkPosition + Directions.NORTH, sectionHeight, Directions.NORTH)
        checkDirection(chunkPosition + Directions.SOUTH, sectionHeight, Directions.SOUTH)
        checkDirection(chunkPosition + Directions.WEST, sectionHeight, Directions.WEST)
        checkDirection(chunkPosition + Directions.EAST, sectionHeight, Directions.EAST)


        visibilityLock.unlock()
    }

    companion object {
        private val DEFAULT_MIN_POSITION = Vec3i.EMPTY
        private val DEFAULT_MAX_POSITION = Vec3i(ProtocolDefinition.SECTION_WIDTH_X, ProtocolDefinition.SECTION_HEIGHT_Y, ProtocolDefinition.SECTION_WIDTH_Z)
    }
}
