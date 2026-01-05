/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
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

import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkUtil.isInViewDistance
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.camera.frustum.FrustumResults
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import kotlin.math.abs

class ChunkVisibilityManager(
    val renderer: ChunkRenderer,
) {
    private val visibility = renderer.context.camera.visibility

    var eyePosition = BlockPosition()
        private set
    var sectionPosition = SectionPosition()
        private set

    private var reasons: IntInlineEnumSet<VisibilityGraphInvalidReason> = IntInlineEnumSet()
    private var viewDistance = renderer.context.session.world.view.viewDistance

    init {
        val state = renderer.context.camera.fog.state
        var end = 0.0f
        state::revision.observe(this) {
            if (state.end < end) {
                reasons += VisibilityGraphInvalidReason.FOG
            }

            end = state.end
        }
    }


    var meshes = VisibleMeshes(this)
        private set


    fun isInViewDistance(position: ChunkPosition): Boolean {
        return position.isInViewDistance(viewDistance, sectionPosition.chunkPosition)
    }

    fun isInViewDistance(position: SectionPosition): Boolean {
        if (abs(position.y - this.sectionPosition.y) > World.MAX_VERTICAL_VIEW_DISTANCE) return false
        return isInViewDistance(position.chunkPosition)
    }

    operator fun contains(position: ChunkPosition) = visibility.isChunkVisible(position)
    operator fun contains(position: SectionPosition) = visibility.isSectionVisible(position, full = false) >= FrustumResults.PARTLY_INSIDE
    operator fun contains(section: ChunkSection) = visibility.isSectionVisible(section)

    fun contains(position: SectionPosition, min: InSectionPosition, max: InSectionPosition) = visibility.isSectionVisible(position, min, max, true)

    private fun collectVisibleMeshes(force: Boolean) {
        this.meshes = VisibleMeshes(this, eyePosition, this.meshes)

        renderer.loaded.forEachVisible { meshes, result ->
            if (force) {
                meshes.resetOcclusion()
            }
            this.meshes.unsafeAdd(meshes, result)
        }

        meshes.sort()
    }

    private fun onVisibilityChange() {
        val eyePosition = renderer.context.session.camera.entity.physics.positionInfo.eyePosition

        if (this.eyePosition != eyePosition) {
            this.eyePosition = eyePosition

            val sectionPosition = eyePosition.sectionPosition
            if (this.sectionPosition != sectionPosition) {
                this.sectionPosition = sectionPosition
                renderer.meshingQueue.tasks.interruptIf(true) { !isInViewDistance(it) }
                renderer.meshingQueue.removeIf(true) { !isInViewDistance(it) }
                renderer.loadingQueue.removeIf(true) { !isInViewDistance(it) }
                renderer.loaded.update()

                renderer.culledQueue.enqueueViewDistance()
            }

            // TODO: remove from meshing queue
            renderer.meshingQueue.sort()
            renderer.loadingQueue.sort()
        }

        renderer.culledQueue.enqueue()
    }


    fun invalidate(reason: VisibilityGraphInvalidReason) {
        this.reasons += reason
    }

    fun update() {
        updateViewDistance() // TODO: delay that for 100ms to not cause rapid loading/unloading

        val reasons = reasons
        if (reasons.size == 0) return

        if (VisibilityGraphInvalidReason.VISIBILITY_GRAPH in reasons) {
            onVisibilityChange()
        }


        // TODO: only recalculate if partly visible, fog/world change, do not if frustum only moved

        var force = false

        if (VisibilityGraphInvalidReason.FOG in reasons || VisibilityGraphInvalidReason.MESH_UPDATE in reasons) { // TODO: only set mesh update if shape changed
            force = true
        }


        collectVisibleMeshes(force)

        this.reasons = IntInlineEnumSet()
    }

    private fun updateViewDistance() {
        val view = renderer.context.session.world.view

        val current = this.viewDistance
        val next = view.viewDistance
        this.viewDistance = next

        when {
            next > current -> renderer.culledQueue.enqueueViewDistance()
            next < current -> {
                renderer.meshingQueue.removeIf(true) { !isInViewDistance(it) }
                renderer.meshingQueue.tasks.interruptIf(true) { !isInViewDistance(it) }
                renderer.loadingQueue.removeIf(true) { !isInViewDistance(it) }
                renderer.loaded.update()
            }
        }
    }
}
