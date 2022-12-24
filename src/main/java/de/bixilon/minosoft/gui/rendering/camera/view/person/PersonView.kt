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

package de.bixilon.minosoft.gui.rendering.camera.view.person

import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.camera.view.CameraView
import de.bixilon.minosoft.gui.rendering.input.camera.MovementInput

interface PersonView : CameraView {
    val camera: Camera

    override fun onInput(input: MovementInput, delta: Double) {
        val isCamera = camera.matrixHandler.entity is LocalPlayerEntity
        camera.context.connection.player.input = if (isCamera) input else MovementInput()
    }

    fun handleMouse(delta: Vec2d): EntityRotation? {
        val entity = camera.matrixHandler.entity
        if (entity !is LocalPlayerEntity) {
            return null
        }
        val rotation = camera.context.inputHandler.cameraInput.calculateRotation(delta, entity.rotation)
        entity.rotation = rotation
        return rotation
    }
}
