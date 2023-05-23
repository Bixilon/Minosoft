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

package de.bixilon.minosoft.gui.rendering.camera.view

import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.input.camera.MovementInputActions
import de.bixilon.minosoft.input.camera.PlayerMovementInput

interface CameraView {
    val context: RenderContext

    val renderSelf: Boolean get() = true
    val renderArm: Boolean get() = false
    val renderOverlays: Boolean get() = false
    val shaking: Boolean get() = true

    val updateFrustum: Boolean get() = true

    val eyePosition: Vec3d
    val matrixPosition: Vec3

    val rotation: EntityRotation
    val front: Vec3

    val fovMultiplier: Float get() = 1.0f


    fun onInput(input: PlayerMovementInput, actions: MovementInputActions, delta: Double) = Unit
    fun onMouse(delta: Vec2d) = Unit
    fun onScroll(scroll: Double) = Unit

    fun onAttach(previous: CameraView?) = Unit
    fun onDetach(next: CameraView) = Unit


    fun draw() = Unit


    companion object {

        fun CameraView.matrix(eye: Vec3d): Vec3 {
            return Vec3(eye - context.camera.offset.offset)
        }
    }
}
