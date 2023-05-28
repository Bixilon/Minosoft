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

package de.bixilon.minosoft.gui.rendering.entity.hitbox

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.entities.LivingEntity
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.entity.ModelUpdater
import de.bixilon.minosoft.gui.rendering.entity.models.EntityModel
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh

class EntityHitbox(
    val model: EntityModel<*>,
) : ModelUpdater {
    private var _mesh: LineMesh? = null
    private var mesh: LineMesh? = null
    private var aabb: AABB = model.aabb
    private var data: HitboxData? = null


    var enabled: Boolean = true
        get() = field && model.renderer.hitboxes
    private var lastEnabled = enabled


    override fun checkUpdate(): Boolean {
        val enabled = enabled
        val lastEnabled = lastEnabled
        this.lastEnabled = enabled
        if (!enabled) {
            return !lastEnabled
        }
        val aabb = model.aabb
        val data = HitboxData.of(model.entity)
        if (lastEnabled && this.aabb == aabb && this.data == data) {
            return false
        }
        this.data = data
        this.aabb = aabb

        return true
    }

    override fun prepareAsync() {
        if (mesh != null) {
            this._mesh = mesh
            mesh = null
        }
        if (!enabled) {
            return
        }
        val data = this.data ?: return

        val visible = ((model.entity.isInvisible && model.renderer.profile.hitbox.showInvisible) || !model.entity.isInvisible) && model.visible && if (model.entity is LivingEntity) model.entity.health > 0.0 else true

        if (!visible) {
            return
        }

        val shrunk = aabb.shrink(0.01f)
        val mesh = LineMesh(model.context)
        if (model.renderer.profile.hitbox.lazy) {
            mesh.drawLazyAABB(shrunk, color = data.color)
        } else {
            mesh.drawAABB(aabb = shrunk, color = data.color, margin = 0.1f)
        }
        val offset = model.context.camera.offset.offset
        val center = Vec3(shrunk.center - offset)



        data.velocity?.let { mesh.drawLine(center, center + Vec3(it) * 3, color = ChatColors.YELLOW) }

        val eyeHeight = shrunk.min.y + model.entity.eyeHeight
        val eyeAABB = AABB(Vec3d(shrunk.min.x, eyeHeight, shrunk.min.z), Vec3d(shrunk.max.x, eyeHeight, shrunk.max.z)).hShrink(-RenderConstants.DEFAULT_LINE_WIDTH)
        mesh.drawAABB(eyeAABB, RenderConstants.DEFAULT_LINE_WIDTH, ChatColors.DARK_RED)


        val eyeStart = Vec3(center.x, eyeHeight - offset.y, center.z)

        mesh.drawLine(eyeStart, eyeStart + data.rotation.front * 5.0f, color = ChatColors.BLUE)
        this.mesh = mesh
    }

    override fun prepare() {
        _mesh?.unload()
        _mesh = null
        val mesh = mesh ?: return
        if (mesh.state == Mesh.MeshStates.PREPARING) {
            mesh.load()
        }
    }

    override fun draw() {
        if (!enabled) {
            return
        }
        mesh?.draw()
    }

    override fun unload() {
        val mesh = mesh
        this.mesh = null
        if (mesh != null && mesh.state == Mesh.MeshStates.LOADED) {
            mesh.unload()
        }
    }
}
