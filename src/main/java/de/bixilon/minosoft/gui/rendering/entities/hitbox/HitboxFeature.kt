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

package de.bixilon.minosoft.gui.rendering.entities.hitbox

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.entities.feature.EntityRenderFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY

class HitboxFeature(renderer: EntityRenderer<*>) : EntityRenderFeature(renderer) {
    private val manager = renderer.renderer.hitbox
    private var mesh: LineMesh? = null

    private var aabb = AABB.EMPTY
    private var eyePosition = Vec3.EMPTY
    private var rotation = EntityRotation.EMPTY

    private val data = HitboxData()
    private var data0 = HitboxData()
    private var data1 = HitboxData()


    override fun reset() {
        unload()
    }

    override fun update(millis: Long) {
        if (!manager.enabled) return unload()

        val offset = renderer.renderer.context.camera.offset.offset

        val update = updateRenderInfo(offset) or interpolate(millis)

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

    private fun interpolate(millis: Long): Boolean {
        // TODO: interpolate color in 5 ticks and velocity per tick
        return false
    }

    private fun updateMesh() {
        unload()
        val mesh = LineMesh(renderer.renderer.context)

        if (manager.profile.lazy) {
            mesh.drawLazyAABB(aabb, data.color)
        } else {
            mesh.drawAABB(aabb, color = data.color)
        }

        val center = Vec3(aabb.center)
        if (data.velocity.length2() > 0.003f) {
            mesh.drawLine(center, center + data.velocity * 15.0f, color = ChatColors.YELLOW)
        }

        mesh.drawLine(eyePosition, eyePosition + rotation.front * 5.0f, color = ChatColors.BLUE)

        this.mesh = mesh
    }


    override fun draw() {
        val mesh = this.mesh ?: return
        if (mesh.state != Mesh.MeshStates.LOADED) mesh.load()
        manager.shader.use()
        mesh.draw()
    }

    override fun unload() {
        val mesh = this.mesh ?: return
        this.mesh = null
        renderer.renderer.queue += { mesh.unload() }
    }
}
