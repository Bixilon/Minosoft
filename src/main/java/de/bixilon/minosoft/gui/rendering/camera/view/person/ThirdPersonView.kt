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
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.gui.rendering.camera.view.CameraView
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.input.camera.MovementInputActions
import de.bixilon.minosoft.input.camera.PlayerMovementInput

// TODO: handle block changes
class ThirdPersonView(
    override val camera: Camera,
    val inverse: Boolean,
) : PersonView {
    override val context: RenderContext get() = camera.context

    override var eyePosition: Vec3d = Vec3d.EMPTY

    override var rotation = EntityRotation.EMPTY
    override var front = Vec3.EMPTY

    override fun onInput(input: PlayerMovementInput, actions: MovementInputActions, delta: Double) {
        super.onInput(input, actions, delta)
        update(eyePosition, front)
    }


    override fun onMouse(delta: Vec2d) {
        val rotation = super.handleMouse(delta) ?: return
        this.rotation = rotation
        update(eyePosition, rotation.front)
    }

    private fun update() {
        val entity = camera.context.connection.camera.entity
        this.rotation = entity.physics.rotation
        update(entity.renderInfo.eyePosition, rotation.front)
    }

    private fun update(position: Vec3d, front: Vec3) {
        val front = if (inverse) -front else front
        val target = camera.context.connection.camera.target.raycastBlock(position, front.toVec3d).first
        val distance = target?.distance?.let { minOf(it, MAX_DISTANCE) } ?: MAX_DISTANCE

        this.eyePosition = if (distance <= 0.0) position else position + (front * (distance - MIN_MARGIN))
        this.front = -front
    }

    override fun onAttach(previous: CameraView?) {
        update()
    }

    override fun draw() {
        update()
    }

    companion object {
        const val MIN_MARGIN = 0.05
        const val MAX_DISTANCE = 3.0
    }
}
