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

package de.bixilon.minosoft.gui.rendering.chunk.queue.queue

import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kmath.vec.vec3.i.Vec3i
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.WorldQueueItem
import de.bixilon.minosoft.gui.rendering.chunk.queue.QueuePosition

class ChunkQueueMaster(
    private val renderer: ChunkRenderer,
) {

    private fun queue(section: ChunkSection, chunk: Chunk, force: Boolean): Boolean {
        val position = SectionPosition.of(chunk.position, section.height)
        if (section.blocks.isEmpty) {
            renderer.unload(QueuePosition(position))
            return false
        }

        val visible = force || renderer.visibility.isSectionVisible(section)
        if (visible) {
            val center = CHUNK_CENTER + BlockPosition.of(chunk.position, section.height)
            val item = WorldQueueItem(position, chunk, section, center)
            renderer.meshingQueue.queue(item)
            return true
        }

        renderer.culledQueue.queue(chunk.position, section.height)

        return false
    }

    fun tryQueue(section: ChunkSection, force: Boolean = false, chunk: Chunk? = null) {
        if (!canQueue()) return
        val chunk = chunk ?: section.chunk
        if (!chunk.neighbours.complete) return

        if (queue(section, chunk, force)) {
            renderer.meshingQueue.sort()
            renderer.meshingQueue.work()
        }
    }

    fun tryQueue(chunk: Chunk?, sectionHeight: SectionHeight, force: Boolean = false) {
        val section = chunk?.get(sectionHeight) ?: return
        tryQueue(section, force, chunk)
    }

    fun tryQueue(chunk: Chunk, ignoreLoaded: Boolean = false, force: Boolean = false) {
        if (!canQueue() || !chunk.neighbours.complete) return
        if (!chunk.neighbours.complete) return

        if (!ignoreLoaded && chunk.position in renderer.loaded) {
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
        for (chunk in world.chunks.chunks.unsafe.values) {
            tryQueue(chunk, ignoreLoaded = true)
        }
        world.lock.release()
    }

    private fun canQueue(): Boolean {
        val state = renderer.context.state
        return !(state == RenderingStates.PAUSED || state == RenderingStates.STOPPED || state == RenderingStates.QUITTING)
    }

    private companion object {
        private val CHUNK_SIZE = Vec3i(ChunkSize.SECTION_MAX_X, ChunkSize.SECTION_MAX_Y, ChunkSize.SECTION_MAX_Z)
        private val CHUNK_CENTER = Vec3f(CHUNK_SIZE) / 2.0f
    }
}
