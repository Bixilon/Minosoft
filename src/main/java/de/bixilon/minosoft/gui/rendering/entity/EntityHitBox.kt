/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.input.camera.frustum.Frustum
import de.bixilon.minosoft.gui.rendering.util.mesh.LineMesh
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh

class EntityHitBox(
    val renderWindow: RenderWindow,
    val entity: Entity,
    val frustum: Frustum,
) {
    private lateinit var mesh: LineMesh
    private var visible = false
    private var aabb = AABB.EMPTY
    private var hitBoxColor = ChatColors.WHITE
    private var checkVisibility = false


    private fun update() {
        val aabb = entity.cameraAABB
        val hitBoxColor = entity.hitBoxColor
        val equals = aabb == this.aabb && hitBoxColor == this.hitBoxColor
        if (equals && !checkVisibility) {
            return
        }
        this.aabb = aabb
        this.hitBoxColor = hitBoxColor
        this.checkVisibility = false

        val visible = ((entity.isInvisible && Minosoft.config.config.game.entities.hitBox.invisibleEntities) || !entity.isInvisible) && frustum.containsAABB(aabb)
        if (checkVisibility && equals && this::mesh.isInitialized) {
            // only visibility changed
            this.visible = visible
            return
        }
        if (this.visible) {
            this.mesh.unload()
        }
        if (visible) {
            val mesh = LineMesh(renderWindow)
            mesh.drawAABB(aabb = aabb, color = hitBoxColor)
            mesh.load()
            this.mesh = mesh
        }
        this.visible = visible
    }

    fun draw() {
        update()
        if (this.visible) {
            mesh.draw()
        }
    }

    fun unload() {
        this.visible = false
        this.aabb = AABB.EMPTY
        if (this::mesh.isInitialized && this.mesh.state == Mesh.MeshStates.LOADED) {
            mesh.unload()
        }
    }

    fun updateVisibility() {
        this.checkVisibility = true
    }
}
