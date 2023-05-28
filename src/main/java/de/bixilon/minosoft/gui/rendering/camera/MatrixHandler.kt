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

package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.kotlinglm.GLM
import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.avg.FloatAverage
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition.CAMERA_UP_VEC3
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition.FAR_PLANE
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition.NEAR_PLANE
import de.bixilon.minosoft.gui.rendering.camera.frustum.Frustum
import de.bixilon.minosoft.gui.rendering.camera.shaking.CameraShaking
import de.bixilon.minosoft.gui.rendering.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.events.CameraPositionChangeEvent
import de.bixilon.minosoft.gui.rendering.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.shader.types.CameraPositionShader
import de.bixilon.minosoft.gui.rendering.shader.types.ViewProjectionShader
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.sectionHeight
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class MatrixHandler(
    private val context: RenderContext,
    private val camera: Camera,
) {
    private val connection = context.connection
    private val profile = context.connection.profiles.rendering.camera
    val shaking = CameraShaking(camera, profile.shaking)
    val frustum = Frustum(camera, this, connection.world)

    private var matrixPosition = Vec3.EMPTY
    private var previousFOV = 0.0f

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

    private var dynamicFOV = FloatAverage(3 * ProtocolDefinition.TICK_TIME * 1_000_000L, 1.0f)

    private fun calculateFOV(): Float {
        var fov = profile.fov

        if (profile.dynamicFOV) {
            fov *= this.dynamicFOV.avg
        }

        fov /= (zoom + 1.0f)

        return fov.clamp(10.0f, 179.9f)
    }


    private fun updateViewMatrix(position: Vec3, front: Vec3) {
        val matrix = Mat4()
        if (camera.view.view.shaking) {
            shaking.transform()?.let { matrix *= it }
        }
        matrix *= GLM.lookAt(position, position + front, CAMERA_UP_VEC3)

        this.viewMatrix = matrix
    }

    private fun calculateProjectionMatrix(fov: Float, screenDimensions: Vec2 = context.window.sizef) {
        projectionMatrix = GLM.perspective(fov.rad, screenDimensions.x / screenDimensions.y, NEAR_PLANE, FAR_PLANE)
    }

    fun init() {
        connection.events.listen<ResizeWindowEvent> {
            calculateProjectionMatrix(calculateFOV(), Vec2(it.size))
            upToDate = false
        }
        draw() // set initial values
    }

    fun draw() {
        dynamicFOV += camera.view.view.fovMultiplier

        val update = shaking.update()
        val fov = calculateFOV()
        val view = camera.view.view
        val eyePosition = view.eyePosition
        context.camera.offset.draw()
        val matrixPosition = Vec3(eyePosition - context.camera.offset.offset)
        val front = view.front
        if (!update && upToDate && eyePosition == this.eyePosition && front == this.front && fov == previousFOV && shaking.isEmpty) {
            return
        }
        this.matrixPosition = matrixPosition
        this.front = front
        val cameraBlockPosition = eyePosition.blockPosition
        if (fov != previousFOV) {
            calculateProjectionMatrix(fov)
        }
        previousFOV = fov

        updateFront(front)
        updateViewMatrix(matrixPosition, front)
        updateViewProjectionMatrix()

        val useMatrixPosition = if (view.updateFrustum) matrixPosition else Vec3(connection.camera.entity.renderInfo.eyePosition - camera.offset.offset)
        val useEyePosition = if (view.updateFrustum) eyePosition else connection.camera.entity.renderInfo.eyePosition

        if (view.updateFrustum) {
            frustum.recalculate()
            camera.visibilityGraph.updateCamera(cameraBlockPosition.chunkPosition, cameraBlockPosition.sectionHeight)
        }

        connection.events.fire(CameraPositionChangeEvent(context, useEyePosition))

        connection.events.fire(
            CameraMatrixChangeEvent(
                context = context,
                viewMatrix = viewMatrix,
                projectionMatrix = projectionMatrix,
                viewProjectionMatrix = viewProjectionMatrix,
            )
        )

        updateShaders(useMatrixPosition)
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
