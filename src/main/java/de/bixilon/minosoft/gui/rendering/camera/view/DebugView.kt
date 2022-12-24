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
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition.CAMERA_UP_VEC3
import de.bixilon.minosoft.gui.rendering.input.camera.MovementInput
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY

class DebugView(private val camera: Camera) : CameraView {
    override val context: RenderContext get() = camera.context
    override val updateFrustum: Boolean get() = false

    override var eyePosition = Vec3.EMPTY

    override var rotation = EntityRotation.EMPTY
    override var front = Vec3.EMPTY


    override fun onInput(input: MovementInput, delta: Double) {
        camera.context.connection.player.input = MovementInput()
        var speedMultiplier = 10
        if (input.sprinting) {
            speedMultiplier *= 3
        }
        if (input.sneaking) {
            speedMultiplier *= 2
        }

        val movement = Vec3.EMPTY

        if (input.movementForward != 0.0f) {
            movement += front * input.movementForward
        }
        if (input.movementSideways != 0.0f) {
            val cameraRight = (CAMERA_UP_VEC3 cross front).normalize()
            movement += cameraRight * input.movementSideways
        }

        if (movement.length2() != 0.0f) {
            movement.normalizeAssign()
        }
        movement *= speedMultiplier
        movement *= delta

        eyePosition = eyePosition + movement
    }

    override fun onMouse(delta: Vec2d) {
        val rotation = context.inputHandler.cameraInput.calculateRotation(delta, this.rotation)
        if (rotation == this.rotation) {
            return
        }
        this.rotation = rotation
        this.front = rotation.front
    }

    override fun onAttach(previous: CameraView?) {
        this.eyePosition = previous?.eyePosition ?: Vec3.EMPTY
        this.rotation = previous?.rotation ?: EntityRotation.EMPTY
        this.front = previous?.front ?: Vec3.EMPTY
    }
}
