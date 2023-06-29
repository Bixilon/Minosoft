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
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.camera.view.CameraView
import de.bixilon.minosoft.input.camera.MovementInputActions
import de.bixilon.minosoft.input.camera.PlayerMovementInput

interface PersonView : CameraView {
    val camera: Camera

    override val fovMultiplier: Float
        get() {
            val base = if (camera.context.connection.camera.entity.isSprinting) 1.3f else 1.0f
            // TODO: item using, movement speed, ...
            return base
        }

    override fun onInput(input: PlayerMovementInput, actions: MovementInputActions, delta: Double) {
        val isCamera = camera.context.connection.camera.entity is LocalPlayerEntity
        val player = camera.context.connection.player

        player.input = if (isCamera) input else PlayerMovementInput()

        val inputActions = player.inputActions
        if (isCamera) {
            player.inputActions = MovementInputActions(
                toggleFly = inputActions.toggleFly || actions.toggleFly,
                startElytraFly = inputActions.startElytraFly || actions.startElytraFly,
            )
        }
    }

    fun handleMouse(delta: Vec2d): EntityRotation? {
        val entity = camera.context.connection.camera.entity
        if (entity !is LocalPlayerEntity) {
            return null
        }
        val rotation = camera.context.inputManager.cameraInput.calculateRotation(delta, entity.physics.rotation)
        entity.physics.forceSetRotation(rotation)
        return rotation
    }
}
