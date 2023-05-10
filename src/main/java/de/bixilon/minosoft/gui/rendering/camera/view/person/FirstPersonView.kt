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

package de.bixilon.minosoft.gui.rendering.camera.view.person

import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.camera.view.CameraView
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY

class FirstPersonView(override val camera: Camera) : PersonView {
    override val context: RenderContext get() = camera.context

    override val renderSelf: Boolean get() = false
    override val renderArm: Boolean
        get() {
            val entity = camera.context.connection.camera.entity
            if (entity is PlayerEntity && entity.gamemode == Gamemodes.SURVIVAL) {
                return false
            }
            return true
        }
    override val renderOverlays: Boolean get() = true

    override var eyePosition: Vec3 = Vec3.EMPTY

    override var rotation = EntityRotation.EMPTY
    override var front = Vec3.EMPTY


    override fun onMouse(delta: Vec2d) {
        val rotation = super.handleMouse(delta) ?: return
        this.rotation = rotation
        this.front = rotation.front
    }

    private fun update() {
        val entity = context.connection.camera.entity
        this.eyePosition = entity.renderInfo.eyePosition
        this.rotation = entity.physics.rotation
        this.front = this.rotation.front
    }

    override fun onAttach(previous: CameraView?) {
        update()
    }

    override fun draw() {
        update()
    }
}
