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

package de.bixilon.minosoft.gui.rendering.camera.view

import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.input.camera.MovementInput
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY

class FirstPersonView(private val camera: Camera) : CameraView {
    override val renderWindow: RenderWindow get() = camera.renderWindow

    override val renderSelf: Boolean get() = false
    override val renderOverlays: Boolean get() = true

    override var eyePosition: Vec3 = Vec3.EMPTY

    override var rotation = EntityRotation.EMPTY
    override var front = Vec3.EMPTY


    override fun onInput(input: MovementInput, delta: Double) {
        val isCamera = camera.matrixHandler.entity is LocalPlayerEntity
        camera.renderWindow.connection.player.input = if (isCamera) input else MovementInput()
    }

    override fun onMouse(delta: Vec2d) {
        val entity = camera.matrixHandler.entity
        if (entity !is LocalPlayerEntity) {
            return
        }
        val rotation = camera.renderWindow.inputHandler.cameraInput.calculateRotation(delta, entity.rotation)
        entity.rotation = rotation
        this.rotation = rotation
        this.front = rotation.front
    }

    private fun update() {
        val entity = camera.matrixHandler.entity
        this.eyePosition = entity.eyePosition
        this.rotation = entity.rotation
        this.front = this.rotation.front
    }

    override fun onAttach(previous: CameraView?) {
        update()
    }

    override fun draw() {
        update()
    }
}
