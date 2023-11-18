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
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.world.view.ViewDistanceChangeEvent
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.feature.EntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.feature.properties.InvisibleFeature.Companion.isInvisible
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class VisibilityManager(val renderer: EntitiesRenderer) {
    private var update = false
    private var _size = 0
    var size: Int = 0
        private set

    val opaque: ArrayList<EntityRenderFeature> = ArrayList(1000)
    val translucent: ArrayList<EntityRenderFeature> = ArrayList(1000)
    private val lock = SimpleLock()
    private val graph = renderer.context.camera.visibilityGraph
    private val frustum = renderer.context.camera.matrixHandler.frustum
    private var renderDistance = 0

    fun init() {
        renderer.connection.events.listen<VisibilityGraphChangeEvent> { update = true }
        renderer.connection.events.listen<ViewDistanceChangeEvent> { updateViewDistance(server = it.viewDistance) }
        renderer.profile.general::renderDistance.observe(this, true) { updateViewDistance(entity = it) }
    }

    private fun updateViewDistance(entity: Int = renderer.profile.general.renderDistance, server: Int = renderer.connection.world.view.serverViewDistance) {
        var distance = if (entity < 0) (server - 1) else entity
        distance *= ProtocolDefinition.SECTION_LENGTH
        this.renderDistance = distance * distance // length^2
    }


    fun reset() {
        opaque.clear()
        translucent.clear()
        size = 0
        _size = 0
    }

    private fun EntityRenderer<*>.isInRenderDistance(): Boolean {
        return renderDistance < 0 || distance <= renderDistance
    }

    fun update(renderer: EntityRenderer<*>, millis: Long) {
        if (!renderer.isInRenderDistance()) {
            // distance is only updated if the renderer is visible, so force update
            renderer.updateRenderInfo(millis)
        }
        if (!renderer.isInRenderDistance()) {
            return renderer.updateVisibility(true, false)
        }
        val aabb = renderer.entity.renderInfo.cameraAABB
        val visible = aabb in frustum
        if (!visible) {
            // TODO: renderer/features: renderOccluded -> occlusion culling is faster than frustum culling
            return renderer.updateVisibility(true, false)
        }
        val occluded = graph.isAABBOccluded(aabb)
        renderer.updateVisibility(occluded, true)
    }

    fun collect(renderer: EntityRenderer<*>) {
        if (!renderer.visible) return
        lock.lock()
        _size++
        for (feature in renderer.features) {
            if (!feature.enabled || !feature.visible || feature.isInvisible()) continue
            feature.collect(this)
        }
        lock.unlock()
    }

    fun finish() {
        // TODO: Optimize it (pre create array, just work with array?)
        this.opaque.sort()
        this.translucent.sort()
        this.update = false
        size = _size
    }

    operator fun get(layer: EntityLayer) = when (layer) {
        EntityLayer.Opaque -> opaque
        EntityLayer.Translucent -> translucent
        else -> throw IllegalStateException("Unknown entity layer: $layer")
    }
}
