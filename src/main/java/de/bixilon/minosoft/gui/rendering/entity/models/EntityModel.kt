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

package de.bixilon.minosoft.gui.rendering.entity.models

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.gui.rendering.entity.EntityHitbox
import de.bixilon.minosoft.gui.rendering.entity.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entity.ModelUpdater
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.world.view.WorldVisibilityGraph

abstract class EntityModel<E : Entity>(
    val renderer: EntityRenderer,
    val entity: E,
) : ModelUpdater {
    val renderWindow = renderer.renderWindow
    open var update = true
    var aabb = AABB.EMPTY
    open var hitbox: EntityHitbox = EntityHitbox(this)
    var visible = false

    override val skipDraw: Boolean
        get() = !visible

    override fun checkUpdate(): Boolean {
        val aabb = entity.cameraAABB
        var update = false
        if (this.aabb != aabb) {
            this.aabb = aabb
            visible = renderer.visibilityGraph.isAABBVisible(aabb)
            update = true
        }
        update = hitbox.checkUpdate() || update

        return update
    }

    override fun prepareAsync() {
        if (!update) {
            return
        }

        hitbox.prepareAsync()

    }

    override fun prepare() {
        if (!update) {
            return
        }
        hitbox.prepare()
        update = false
    }

    override fun draw() {
        drawHitbox()
    }

    override fun unload() {
        hitbox.unload()
    }

    open fun updateVisibility(graph: WorldVisibilityGraph) {
        visible = graph.isAABBVisible(aabb)
    }

    protected open fun drawHitbox() {
        if (!hitbox.enabled) {
            return
        }
        if (renderer.profile.hitbox.showThroughWalls) {
            renderWindow.renderSystem.reset(faceCulling = false, depth = DepthFunctions.ALWAYS)
        } else {
            renderWindow.renderSystem.reset(faceCulling = false)
        }

        renderWindow.shaderManager.genericColorShader.use()

        hitbox.draw()
    }
}
