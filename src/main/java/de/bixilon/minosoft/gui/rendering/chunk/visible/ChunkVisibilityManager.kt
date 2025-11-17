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

package de.bixilon.minosoft.gui.rendering.chunk.visible

import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkUtil.isInViewDistance
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer

class ChunkVisibilityManager(
    val renderer: ChunkRenderer,
) {
    private val visibility = renderer.context.camera.visibility

    var eyePosition = BlockPosition()
        private set
    var sectionPosition = SectionPosition()
        private set

    private var invalid = true
    private var viewDistance = renderer.context.session.world.view.viewDistance


    var meshes = VisibleMeshes()
        private set


    fun isInViewDistance(position: ChunkPosition): Boolean {
        return position.isInViewDistance(viewDistance, sectionPosition.chunkPosition)
    }

    fun isInViewDistance(position: SectionPosition) = isInViewDistance(position.chunkPosition) // TODO: vertical view distance

    operator fun contains(position: ChunkPosition) = visibility.isChunkVisible(position)
    operator fun contains(position: SectionPosition) = visibility.isSectionVisible(position)
    operator fun contains(section: ChunkSection) = visibility.isSectionVisible(section)

    fun contains(position: SectionPosition, min: InSectionPosition, max: InSectionPosition) = visibility.isSectionVisible(position, min, max)

    private fun onVisibilityChange() {
        val eyePosition = renderer.context.session.camera.entity.physics.positionInfo.eyePosition

        if (this.eyePosition != eyePosition) {
            this.eyePosition = eyePosition

            val sectionPosition = eyePosition.sectionPosition
            if (this.sectionPosition != sectionPosition) {
                this.sectionPosition = sectionPosition
                renderer.meshingQueue.tasks.interruptIf { !isInViewDistance(it) }
                renderer.meshingQueue.removeIf { !isInViewDistance(it) }
                renderer.loadingQueue.removeIf { !isInViewDistance(it) }
                renderer.loaded.update()

                renderer.culledQueue.enqueueViewDistance()
            }

            // TODO: remove from meshing queue
            renderer.meshingQueue.sort()
            renderer.loadingQueue.sort()
        }
        renderer.culledQueue.enqueue()

        this.meshes = VisibleMeshes(eyePosition, this.meshes)
        renderer.loaded.addTo(meshes)
        meshes.sort()
    }


    fun invalidate() {
        invalid = true
    }

    fun update() {
        updateViewDistance() // TODO: delay that for 100ms to not cause rapid loading/unloading

        if (invalid) {
            onVisibilityChange()
            invalid = false
        }
    }

    private fun updateViewDistance() {
        val view = renderer.context.session.world.view

        val current = this.viewDistance
        val next = view.viewDistance
        this.viewDistance = next

        when {
            next > current -> renderer.culledQueue.enqueueViewDistance()
            next < current -> {
                renderer.meshingQueue.removeIf { !isInViewDistance(it) }
                renderer.meshingQueue.tasks.interruptIf { !isInViewDistance(it) }
                renderer.loadingQueue.removeIf { !isInViewDistance(it) }
                renderer.loaded.update()
            }
        }
    }
}
