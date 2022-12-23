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

package de.bixilon.minosoft.gui.rendering.world.queue.queue

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.toVec3
import de.bixilon.minosoft.gui.rendering.world.WorldQueueItem
import de.bixilon.minosoft.gui.rendering.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.world.queue.QueuePosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class ChunkQueueMaster(
    private val renderer: WorldRenderer,
) {

    private fun queue(section: ChunkSection, chunk: Chunk, force: Boolean): Boolean {
        if (section.blocks.isEmpty) {
            renderer.unload(QueuePosition(chunk.chunkPosition, section.sectionHeight))
            return false
        }

        val visible = force || renderer.visibilityGraph.isSectionVisible(chunk.chunkPosition, section.sectionHeight, section.blocks.minPosition, section.blocks.maxPosition, true)
        if (visible) {
            val center = Vec3i.of(chunk.chunkPosition, section.sectionHeight).toVec3() + CHUNK_CENTER
            val item = WorldQueueItem(chunk.chunkPosition, section.sectionHeight, chunk, section, center)
            renderer.meshingQueue.queue(item)
            return true
        }

        renderer.culledQueue.queue(chunk.chunkPosition, section.sectionHeight)

        return false
    }

    fun tryQueue(section: ChunkSection, force: Boolean = false, chunk: Chunk? = null) {
        if (!canQueue()) return
        val chunk = chunk ?: section.chunk ?: return
        if (!chunk.isFullyLoaded) return

        chunk.neighbours.get() ?: return

        if (queue(section, chunk, force)) {
            renderer.meshingQueue.sort()
            renderer.meshingQueue.work()
        }
    }

    fun tryQueue(chunk: Chunk, sectionHeight: SectionHeight, force: Boolean = false) {
        val section = chunk[sectionHeight] ?: return
        tryQueue(section, force, chunk)
    }

    fun tryQueue(chunk: Chunk, ignoreLoaded: Boolean = false, force: Boolean = false) {
        if (!canQueue() || !chunk.isFullyLoaded) return
        chunk.neighbours.get() ?: return

        if (!ignoreLoaded && chunk.chunkPosition in renderer.loaded) {
            // chunks only get queued when the server sends them, we normally do not want to queue them again.
            // that happens when e.g. light data arrives
            return
        }

        var changes = 0
        for (sectionHeight in chunk.minSection..chunk.maxSection) { // TODO .. or until?
            val section = chunk[sectionHeight] ?: continue
            if (queue(section, chunk, force)) {
                changes++
            }
        }
        if (changes > 0) {
            renderer.meshingQueue.sort()
            renderer.meshingQueue.work()
        }
    }

    fun tryQueue(world: World) {
        world.lock.acquire()
        for (chunk in world.chunks.unsafe.values) {
            tryQueue(chunk, ignoreLoaded = true)
        }
        world.lock.release()
    }

    private fun canQueue(): Boolean {
        val state = renderer.renderWindow.state
        if (state == RenderingStates.PAUSED || state == RenderingStates.STOPPED || state == RenderingStates.QUITTING) {
            return false
        }
        return true
    }

    private companion object {
        private val CHUNK_SIZE = Vec3i(ProtocolDefinition.SECTION_MAX_X, ProtocolDefinition.SECTION_MAX_Y, ProtocolDefinition.SECTION_MAX_Z)
        private val CHUNK_CENTER = Vec3(CHUNK_SIZE) / 2.0f
    }
}
