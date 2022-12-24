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

package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.kotlinglm.GLM
import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition.CAMERA_UP_VEC3
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition.FAR_PLANE
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition.NEAR_PLANE
import de.bixilon.minosoft.gui.rendering.camera.frustum.Frustum
import de.bixilon.minosoft.gui.rendering.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.events.CameraPositionChangeEvent
import de.bixilon.minosoft.gui.rendering.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.shader.types.CameraPositionShader
import de.bixilon.minosoft.gui.rendering.shader.types.ViewProjectionShader
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.sectionHeight
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen

class MatrixHandler(
    private val context: RenderContext,
    private val fogManager: FogManager,
    private val camera: Camera,
) {
    private val connection = context.connection
    private val profile = context.connection.profiles.rendering.camera
    val frustum = Frustum(this, connection.world)

    @Deprecated("outsource to connection")
    var entity: Entity = context.connection.player
        set(value) {
            field = value
            upToDate = false
        }

    private var eyePosition = Vec3.EMPTY
    private var previousFOV = 0.0

    private var front = Vec3.EMPTY
    private var right = Vec3(0.0, 0.0, -1.0)
    private var up = Vec3(0.0, 1.0, 0.0)

    var zoom = 0.0f
        set(value) {
            field = value
            upToDate = false
        }

    private var upToDate = false

    var viewMatrix = Mat4()
        private set
    var projectionMatrix = Mat4()
        private set
    var viewProjectionMatrix = projectionMatrix * viewMatrix
        private set

    private val fov: Double
        get() {
            val fov = profile.fov / (zoom + 1.0)

            if (!profile.dynamicFOV) {
                return fov
            }
            return fov * connection.player.fovMultiplier.interpolate()
        }


    private fun updateViewMatrix(position: Vec3, front: Vec3) {
        viewMatrix = GLM.lookAt(position, position + front, CAMERA_UP_VEC3)
    }

    private fun calculateProjectionMatrix(screenDimensions: Vec2 = context.window.sizef) {
        projectionMatrix = GLM.perspective(fov.rad.toFloat(), screenDimensions.x / screenDimensions.y, NEAR_PLANE, FAR_PLANE)
    }

    fun init() {
        connection.events.listen<ResizeWindowEvent> {
            calculateProjectionMatrix(Vec2(it.size))
            upToDate = false
        }
        draw() // set initial values
    }

    fun draw() {
        val fov = fov
        val view = camera.view.view
        val eyePosition = view.eyePosition
        val front = view.front
        if (upToDate && eyePosition == this.eyePosition && front == this.front && fov == previousFOV) {
            return
        }
        this.eyePosition = eyePosition
        this.front = front
        val cameraBlockPosition = eyePosition.blockPosition
        if (fov != previousFOV) {
            calculateProjectionMatrix()
        }
        previousFOV = fov

        updateFront(front)
        updateViewMatrix(eyePosition, front)
        updateViewProjectionMatrix()

        val usePosition = if (view.updateFrustum) eyePosition else entity.eyePosition

        if (view.updateFrustum) {
            frustum.recalculate()
            camera.visibilityGraph.updateCamera(cameraBlockPosition.chunkPosition, cameraBlockPosition.sectionHeight)
        }

        connection.events.fire(CameraPositionChangeEvent(context, usePosition))

        connection.events.fire(CameraMatrixChangeEvent(
            context = context,
            viewMatrix = viewMatrix,
            projectionMatrix = projectionMatrix,
            viewProjectionMatrix = viewProjectionMatrix,
        )
        )

        updateShaders(usePosition)
        upToDate = true
    }

    private fun updateViewProjectionMatrix() {
        viewProjectionMatrix = projectionMatrix * viewMatrix
    }

    private fun updateFront(front: Vec3) {
        this.front = front
        this.right = (front cross CAMERA_UP_VEC3).normalize()
        this.up = (this.right cross front).normalize()
    }

    private fun updateShaders(cameraPosition: Vec3) {
        for (shader in context.renderSystem.shaders) {
            if (shader is ViewProjectionShader) {
                shader.viewProjectionMatrix = viewProjectionMatrix
            }
            if (shader is CameraPositionShader) {
                shader.cameraPosition = cameraPosition
            }
        }
    }
}
