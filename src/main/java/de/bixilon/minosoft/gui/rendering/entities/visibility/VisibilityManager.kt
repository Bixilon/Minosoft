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

package de.bixilon.minosoft.gui.rendering.entities.visibility

import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.feature.EntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import java.util.concurrent.atomic.AtomicInteger

class VisibilityManager(val renderer: EntitiesRenderer) {
    private var update = false
    var size: Int = 0
        private set

    private val count = AtomicInteger()
    val opaque: ArrayList<EntityRenderFeature> = ArrayList(1000)
    val translucent: ArrayList<EntityRenderFeature> = ArrayList(1000)
    private val lock = SimpleLock()
    private val graph = renderer.context.camera.visibilityGraph
    private val frustum = renderer.context.camera.matrixHandler.frustum

    fun init() {
        renderer.connection.events.listen<VisibilityGraphChangeEvent> { update = true }
    }

    fun reset() {
        opaque.clear()
        translucent.clear()
        count.set(0)
    }

    fun update(renderer: EntityRenderer<*>) {
        // TODO: check render distance (and maybe entity distance)
        val aabb = renderer.entity.renderInfo.cameraAABB
        val visible = aabb in frustum
        if (!visible) {
            // TODO: renderer/features: renderOccluded -> occlusion culling is faster than frustum culling
            return renderer.updateVisibility(true, true)
        }
        val occluded = graph.isAABBOccluded(aabb)
        renderer.updateVisibility(occluded, true)
    }

    fun collect(renderer: EntityRenderer<*>) {
        if (!renderer.visible) return
        count.incrementAndGet()
        lock.lock()
        for (feature in renderer.features) {
            if (!feature.enabled || !feature.visible) continue
            feature.collect(this)
        }
        lock.unlock()
    }

    fun finish() {
        // TODO: Optimize it (pre create array, just work with array?)
        this.opaque.sort()
        this.translucent.sort()
        this.update = false
        size = count.get()
    }

    operator fun get(layer: EntityLayer) = when (layer) {
        EntityLayer.Opaque -> opaque
        EntityLayer.Translucent -> translucent
        else -> throw IllegalStateException("Unknown entity layer: $layer")
    }
}
