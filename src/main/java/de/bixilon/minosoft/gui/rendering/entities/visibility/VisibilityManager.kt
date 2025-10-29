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

package de.bixilon.minosoft.gui.rendering.entities.visibility

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.view.ViewDistanceChangeEvent
import de.bixilon.minosoft.gui.rendering.entities.EntitiesRenderer
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.events.VisibilityGraphChangeEvent
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen

class VisibilityManager(val renderer: EntitiesRenderer) {
    private var invalid = false // TODO: Use
    private val graph = renderer.context.camera.occlusion
    private val frustum = renderer.context.camera.frustum
    private var renderDistance = 0
    private var eyePosition = renderer.session.camera.entity.physics.positionInfo.eyePosition

    fun init() {
        renderer.session.events.listen<VisibilityGraphChangeEvent> { invalid = true }
        renderer.session.events.listen<ViewDistanceChangeEvent> { updateViewDistance(server = it.viewDistance) }
        renderer.profile.general::renderDistance.observe(this, true) { updateViewDistance(entity = it) }
    }

    private fun updateViewDistance(entity: Int = renderer.profile.general.renderDistance, server: Int = renderer.session.world.view.serverViewDistance) {
        var distance = if (entity < 0) (server - 1) else entity
        distance *= ChunkSize.SECTION_LENGTH
        this.renderDistance = distance * distance // length^2
    }


    fun update() {
        eyePosition = renderer.session.camera.entity.physics.positionInfo.eyePosition
    }

    private fun getVisibility(renderer: EntityRenderer<*>): EntityVisibility {
        val distance = Vec3iUtil.distance2(renderer.entity.physics.positionInfo.eyePosition, eyePosition).toDouble()
        if (distance >= renderDistance) return EntityVisibility.OUT_OF_VIEW_DISTANCE

        val entity = renderer.entity
        val aabb = if (!renderer.isVisible()) entity.physics.aabb else entity.renderInfo.cameraAABB // cameraAABB is only updated if entity is visible

        return when {
            // TODO: renderer/features: renderOccluded -> occlusion culling is faster than frustum culling
            aabb !in frustum -> EntityVisibility.OUT_OF_FRUSTUM
            graph.isAABBOccluded(aabb) -> EntityVisibility.OCCLUDED
            else -> EntityVisibility.VISIBLE
        }
    }


    fun update(renderer: EntityRenderer<*>) {
        val visibility = getVisibility(renderer)
        renderer.updateVisibility(visibility)
    }
}
