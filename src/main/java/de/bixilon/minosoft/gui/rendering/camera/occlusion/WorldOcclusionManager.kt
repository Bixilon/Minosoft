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

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.ForcePooledRunnable
import de.bixilon.kutil.math.simple.IntMath.clamp
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateEvent
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkUnloadUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.NeighbourSetUpdate
import de.bixilon.minosoft.data.world.container.block.occlusion.SectionOcclusion
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen

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

    private var queue = false


    init {
        session.world::occlusion.observe(this) { invalidate() }

        session.events.listen<WorldUpdateEvent> {
            if (it.update !is NeighbourSetUpdate && it.update !is ChunkUnloadUpdate) {
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

        val min = aabb.min.blockPosition.sectionPosition
        val max = aabb.max.blockPosition.sectionPosition

        for (y in min.y..max.y) {
            for (z in min.z..max.z) {
                for (x in min.x..max.x) {
                    if (graph.isOccluded(SectionPosition(x, y, z))) continue

                    return false
                }
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

    private fun workQueue(queue: Set<SectionOcclusion>) {
        if (queue.isEmpty()) return
        if (this.queue) return // already working on it

        this.queue = true
        DefaultThreadPool += ForcePooledRunnable(priority = ThreadPool.Priorities.HIGH) {
            for (occlusion in queue) {
                occlusion.calculate()
            }
            this.queue = false
        }
    }


    override fun draw() {
        if (graph != null) return


        val world = context.session.world
        val chunk = world.chunks[this.position.chunkPosition] ?: return // TODO: optimize camera chunk retrieval
        val viewDistance = world.view.viewDistance

        val tracer = OcclusionTracer(this.position, world.dimension, camera, viewDistance)
        this.graph = tracer.trace(chunk)

        workQueue(tracer.queue)


        session.events.fire(VisibilityGraphChangeEvent(context))
    }
}
