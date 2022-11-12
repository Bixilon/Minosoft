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
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.gui.rendering.RenderWindow
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
    private val renderWindow: RenderWindow,
    private val fogManager: FogManager,
    private val camera: Camera,
) {
    private val connection = renderWindow.connection
    private val profile = renderWindow.connection.profiles.rendering.camera
    val frustum = Frustum(this, connection.world)
    var entity: Entity = renderWindow.connection.player
        set(value) {
            field = value
            upToDate = false
        }


    var eyePosition = Vec3.EMPTY
        private set
    var rotation = EntityRotation(0.0, 0.0)
        private set
    private var previousFOV = 0.0

    var cameraFront = Vec3(0.0, 0.0, -1.0)
        private set
    var cameraRight = Vec3(0.0, 0.0, -1.0)
        private set
    var cameraUp = Vec3(0.0, 1.0, 0.0)
        private set


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

    private var previousDebugView = false
    private var previousDebugPosition = Vec3.EMPTY
    private var previousDebugRotation = EntityRotation(0.0, 0.0)
    var debugPosition = Vec3.EMPTY
    var debugRotation = EntityRotation(0.0, 0.0)


    private val fov: Double
        get() {
            val fov = profile.fov / (zoom + 1.0)

            if (!profile.dynamicFOV) {
                return fov
            }
            return fov * connection.player.fovMultiplier.interpolate()
        }


    private fun calculateViewMatrix(eyePosition: Vec3 = entity.eyePosition) {
        viewMatrix = GLM.lookAt(eyePosition, eyePosition + cameraFront, CAMERA_UP_VEC3)
    }

    private fun calculateProjectionMatrix(screenDimensions: Vec2 = renderWindow.window.sizef) {
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
        val debugView = camera.debugView
        val fov = fov
        val eyePosition = entity.eyePosition
        val rotation = entity.rotation
        val debugPosition = debugPosition
        val debugRotation = debugRotation
        if ((upToDate && eyePosition == this.eyePosition && rotation == this.rotation && fov == previousFOV) && previousDebugView == debugView && (!debugView || (previousDebugPosition == debugPosition && previousDebugRotation == debugRotation))) {
            return
        }
        this.previousDebugView = debugView
        this.previousDebugPosition = debugPosition
        this.previousDebugRotation = debugRotation
        this.eyePosition = eyePosition
        this.rotation = rotation
        val cameraBlockPosition = eyePosition.blockPosition
        if (fov != previousFOV) {
            calculateProjectionMatrix()
        }
        previousFOV = fov

        updateRotation(if (debugView) debugRotation else rotation)
        updateViewMatrix(if (debugView) debugPosition else eyePosition)
        updateViewProjectionMatrix()

        if (!debugView) {
            frustum.recalculate()
            camera.visibilityGraph.updateCamera(cameraBlockPosition.chunkPosition, cameraBlockPosition.sectionHeight)
        }

        connection.fire(CameraPositionChangeEvent(renderWindow, eyePosition))

        connection.fire(
            CameraMatrixChangeEvent(
                renderWindow = renderWindow,
                viewMatrix = viewMatrix,
                projectionMatrix = projectionMatrix,
                viewProjectionMatrix = viewProjectionMatrix,
            )
        )

        updateShaders(if (debugView) debugPosition else eyePosition)
        upToDate = true
    }

    private fun updateViewMatrix(eyePosition: Vec3) {
        calculateViewMatrix(eyePosition)
    }

    private fun updateViewProjectionMatrix() {
        viewProjectionMatrix = projectionMatrix * viewMatrix
    }

    private fun updateRotation(rotation: EntityRotation = entity.rotation) {
        cameraFront = rotation.front

        cameraRight = (cameraFront cross CAMERA_UP_VEC3).normalize()
        cameraUp = (cameraRight cross cameraFront).normalize()
    }

    private fun updateShaders(cameraPosition: Vec3) {
        for (shader in renderWindow.renderSystem.minosoftShaders) {
            if (shader is ViewProjectionShader) {
                shader.viewProjectionMatrix = viewProjectionMatrix
            }
            if (shader is CameraPositionShader) {
                shader.cameraPosition = cameraPosition
            }
        }
    }

    companion object {
        const val NEAR_PLANE = 0.01f
        const val FAR_PLANE = 10000.0f
        val CAMERA_UP_VEC3 = Vec3(0.0, 1.0, 0.0)
    }
}
