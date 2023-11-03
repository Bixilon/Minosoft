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

package de.bixilon.minosoft.gui.rendering.entities.feature.hitbox

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.ColorUtil
import de.bixilon.minosoft.gui.rendering.entities.feature.EntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.interpolate.Interpolator

class HitboxFeature(renderer: EntityRenderer<*>) : EntityRenderFeature(renderer) {
    private val manager = renderer.renderer.features.hitbox
    private var mesh: LineMesh? = null

    private var aabb = AABB.EMPTY
    private var eyePosition = Vec3.EMPTY
    private var rotation = EntityRotation.EMPTY

    private var color = Interpolator(renderer.entity.hitboxColor ?: ChatColors.WHITE, ColorUtil::interpolateRGB)
    private var velocity = Interpolator(Vec3.EMPTY, Vec3Util::interpolateLinear)


    override fun reset() {
        unload()
    }

    override fun update(millis: Long, delta: Float) {
        if (!manager.enabled) return unload()
        if (!enabled) return unload()
        if (!visible) return

        val offset = renderer.renderer.context.camera.offset.offset

        val update = updateRenderInfo(offset) or interpolate(delta)

        if (this.mesh != null && !update) return

        updateMesh()
    }


    private fun updateRenderInfo(offset: Vec3i): Boolean {
        var changes = 0

        val renderInfo = renderer.entity.renderInfo
        val aabb = renderInfo.cameraAABB + -offset
        val eyePosition = Vec3(renderInfo.eyePosition - offset)
        val rotation = renderInfo.rotation

        if (aabb != this.aabb) {
            this.aabb = aabb; changes++
        }
        if (eyePosition != this.eyePosition) {
            this.eyePosition = eyePosition; changes++
        }
        if (rotation != this.rotation) {
            this.rotation = rotation; changes++
        }

        return changes > 0
    }

    private fun interpolate(delta: Float): Boolean {
        if (color.delta >= 1.0f) {
            this.color.push(renderer.entity.hitboxColor ?: ChatColors.WHITE)
        }
        this.color.add(delta, 0.3f)

        if (velocity.delta >= 1.0f) {
            this.velocity.push(Vec3(renderer.entity.physics.velocity))
        }
        this.velocity.add(delta, ProtocolDefinition.TICK_TIME / 1000.0f)


        return !this.color.identical || !this.velocity.identical
    }

    private fun updateMesh() {
        unload()
        val mesh = LineMesh(renderer.renderer.context)

        val color = color.value
        if (manager.profile.lazy) {
            mesh.drawLazyAABB(aabb, color)
        } else {
            mesh.drawAABB(aabb, color = color)
        }

        val center = Vec3(aabb.center)
        val velocity = velocity.value
        if (velocity.length2() > 0.003f) {
            mesh.drawLine(center, center + velocity * 5.0f, color = ChatColors.YELLOW)
        }

        mesh.drawLine(eyePosition, eyePosition + rotation.front * 5.0f, color = ChatColors.BLUE)

        this.mesh = mesh
    }


    override fun draw() {
        val mesh = this.mesh ?: return
        if (mesh.state != Mesh.MeshStates.LOADED) mesh.load()
        val system = renderer.renderer.context.system
        if (manager.profile.showThroughWalls) {
            system.reset(depth = DepthFunctions.ALWAYS)
        } else {
            system.reset()
        }
        manager.shader.use()
        mesh.draw()
    }

    override fun unload() {
        val mesh = this.mesh ?: return
        this.mesh = null
        renderer.renderer.queue += { mesh.unload() }
    }
}
