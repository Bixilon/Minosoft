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

package de.bixilon.minosoft.gui.rendering.camera.occlusion

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateEvent
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkCreateUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkUnloadUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.NeighbourChangeUpdate
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

/**
 * Handles visibility of objects
 *
 * Credit for occlusion culling:
 *  - https://github.com/stackotter/delta-client (with big thanks to @stackotter for ideas and explanation!)
 *  - https://tomcc.github.io/2014/08/31/visibility-1.html
 */
class WorldOcclusionManager(
    private val context: RenderContext,
    val camera: Camera,
) : Drawable {
    private val session = context.session
    private var graph: OcclusionGraph? = null
    private var position = SectionPosition.EMPTY


    init {
        session.world::occlusion.observe(this) { invalidate() }

        session.events.listen<WorldUpdateEvent> {
            if (it.update !is ChunkCreateUpdate && it.update !is NeighbourChangeUpdate && it.update !is ChunkUnloadUpdate) {
                return@listen
            }
            invalidate()
        }
    }

    private fun invalidate() {
        graph = null
    }

    fun isAABBOccluded(aabb: AABB): Boolean {
        if (!RenderConstants.OCCLUSION_CULLING_ENABLED) return false
        val graph = this.graph ?: return false

        val positions: MutableSet<ChunkPosition> = HashSet(4, 1.0f)
        val heights = IntOpenHashSet()
        for (position in aabb.positions()) { // TODO: use ChunkPosition iterator
            positions += position.chunkPosition
            heights += position.sectionHeight
        }

        for (position in positions) {
            val iterator = heights.intIterator()
            while (iterator.hasNext()) {
                val height = iterator.nextInt()
                if (graph.isOccluded(SectionPosition.of(position, height))) continue

                return false
            }
        }
        return true
    }

    fun isSectionOccluded(position: SectionPosition): Boolean {
        if (!RenderConstants.OCCLUSION_CULLING_ENABLED) return false
        val graph = this.graph ?: return false
        return graph.isOccluded(position)
    }

    fun update(position: SectionPosition) {
        val dimension = context.session.world.dimension
        this.position = position.with(y = position.y.clamp(dimension.minSection - 1, dimension.maxSection + 1)) // prevent unneeded tracing to chunks
        graph = null
    }


    override fun draw() {
        if (graph != null) return


        val world = context.session.world
        val size = context.session.world.chunks.size.size.size
        val chunk = context.session.world.chunks[this.position.chunkPosition] ?: return // TODO: optimize camera chunk retrieval

        this.graph = OcclusionTracer(this.position, size, world.dimension, camera).trace(chunk)
        session.events.fire(VisibilityGraphChangeEvent(context))
    }
}
