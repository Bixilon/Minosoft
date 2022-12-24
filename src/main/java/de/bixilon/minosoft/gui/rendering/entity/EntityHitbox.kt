/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.entity

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.registries.shapes.AABB
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.entity.models.EntityModel
import de.bixilon.minosoft.gui.rendering.util.VecUtil.empty
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY

class EntityHitbox(
    val model: EntityModel<*>,
) : ModelUpdater {
    private var _mesh: LineMesh? = null
    private var mesh: LineMesh? = null
    private var aabb = AABB.EMPTY
    private var color = ChatColors.WHITE
    private var velocity = Vec3d.EMPTY
    private var rotation = EntityRotation.EMPTY

    private var update = true

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
        val entity = model.entity
        val color = entity.hitboxColor
        val velocity = entity.velocity
        val rotation = entity.rotation
        val equals = color == this.color && this.velocity == velocity && this.rotation == rotation && !lastEnabled
        if (equals) {
            return false
        }
        this.color = color
        this.velocity = velocity
        this.rotation = rotation
        update = true

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
        val aabb = model.aabb
        this.aabb = aabb

        val visible = ((model.entity.isInvisible && model.renderer.profile.hitbox.showInvisible) || !model.entity.isInvisible) && model.visible

        if (!visible) {
            return
        }

        val shrunk = aabb.shrink(0.01f)
        val mesh = LineMesh(model.context)
        if (model.renderer.profile.hitbox.lazy) {
            mesh.drawLazyAABB(shrunk, color = color)
        } else {
            mesh.drawAABB(aabb = shrunk, color = color, margin = 0.1f)
        }
        val center = Vec3(shrunk.center)

        if (!velocity.empty) {
            mesh.drawLine(center, center + Vec3(velocity) * 3, color = ChatColors.YELLOW)
        }


        val eyeHeight = shrunk.min.y + model.entity.eyeHeight
        val eyeAABB = AABB(Vec3(shrunk.min.x, eyeHeight, shrunk.min.z), Vec3(shrunk.max.x, eyeHeight, shrunk.max.z)).hShrink(RenderConstants.DEFAULT_LINE_WIDTH)
        mesh.drawAABB(eyeAABB, RenderConstants.DEFAULT_LINE_WIDTH, ChatColors.DARK_RED)


        val eyeStart = Vec3(center.x, eyeHeight, center.z)

        mesh.drawLine(eyeStart, eyeStart + Vec3(rotation.front) * 5, color = ChatColors.BLUE)
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
