/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kmath.mat.mat4.f.MMat4f
import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.avg._float.FloatAverage
import de.bixilon.kutil.math.simple.FloatMath.clamp
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.primitive.FloatUtil.rad
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition.CAMERA_UP_VEC3
import de.bixilon.minosoft.gui.rendering.camera.CameraDefinition.NEAR_PLANE
import de.bixilon.minosoft.gui.rendering.camera.shaking.CameraShaking
import de.bixilon.minosoft.gui.rendering.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.events.CameraPositionChangeEvent
import de.bixilon.minosoft.gui.rendering.shader.types.CameraPositionShader
import de.bixilon.minosoft.gui.rendering.shader.types.ViewProjectionShader
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.blockPosition
import de.bixilon.minosoft.protocol.network.session.play.tick.Ticks.Companion.ticks

class MatrixHandler(
    private val context: RenderContext,
    private val camera: Camera,
) {
    private val session = context.session
    private val profile = context.session.profiles.rendering.camera
    val shaking = CameraShaking(camera, profile.shaking)

    private var matrixPosition = Vec3f.EMPTY
    private var previousFOV = 0.0f

    private var front = Vec3f.EMPTY
    private var right = Vec3f(0.0f, 0.0f, -1.0f)
    private var up = Vec3f(0.0f, 1.0f, 0.0f)

    var zoom = 0.0f
        set(value) {
            field = value
            invalidate()
        }

    private var invalid = false

    var viewMatrix = Mat4f()
        private set
    var projectionMatrix = Mat4f()
        private set
    var viewProjectionMatrix = projectionMatrix * viewMatrix
        private set

    private var dynamicFOV = FloatAverage(3.ticks.duration, 1.0f)

    private fun calculateFOV(): Float {
        var fov = profile.fov

        if (profile.dynamicFOV) {
            fov *= this.dynamicFOV.avg
        }

        fov /= (zoom + 1.0f)

        return fov.clamp(10.0f, 179.9f)
    }


    private fun updateViewMatrix(position: Vec3f, front: Vec3f) {
        val matrix = MMat4f()
        if (camera.view.view.shaking) {
            shaking.transform()?.let { matrix *= it }
        }
        matrix *= CameraUtil.lookAt(position, position + front, CAMERA_UP_VEC3)

        this.viewMatrix = matrix.unsafe
    }

    private fun calculateProjectionMatrix(fov: Float, screenDimensions: Vec2f = Vec2f(context.window.size)) {
        val fog = camera.fog.state
        var far = (session.world.view.viewDistance + 1) * ChunkSize.SECTION_LENGTH.toFloat()
        if (fog.enabled) {
            far = fog.end * (1.0f / 0.7f) + 2.0f // y axis is weighted differently
        }
        projectionMatrix = CameraUtil.perspective(fov.rad, screenDimensions.x / screenDimensions.y, NEAR_PLANE, maxOf(far, 5.0f))
    }

    fun init() {
        context.window::size.observe(this, true) {
            calculateProjectionMatrix(calculateFOV(), Vec2f(it))
            invalidate()
        }
        draw() // set initial values
    }

    fun draw() {
        dynamicFOV += camera.view.view.fovMultiplier

        shaking.draw()
        val fov = calculateFOV()
        val view = camera.view.view
        val eyePosition = view.eyePosition
        context.camera.offset.draw()
        val matrixPosition = Vec3f(eyePosition - context.camera.offset.offset)
        val front = view.front
        if (!invalid && matrixPosition == this.matrixPosition && front == this.front && fov == previousFOV && shaking.isEmpty) {
            return
        }
        this.matrixPosition = matrixPosition
        this.front = front
        val cameraBlockPosition = eyePosition.blockPosition
        calculateProjectionMatrix(fov)
        previousFOV = fov

        updateFront(front)
        updateViewMatrix(matrixPosition, front)
        updateViewProjectionMatrix()

        val useMatrixPosition = if (view.updateFrustum) matrixPosition else Vec3f(session.camera.entity.renderInfo.eyePosition - camera.offset.offset)
        val useEyePosition = if (view.updateFrustum) eyePosition else session.camera.entity.renderInfo.eyePosition

        if (view.updateFrustum) {
            camera.frustum.recalculate()
            camera.occlusion.update(cameraBlockPosition.sectionPosition)
        }

        session.events.fire(CameraPositionChangeEvent(context, useEyePosition))

        session.events.fire(CameraMatrixChangeEvent(
            context = context,
            viewMatrix = viewMatrix,
            projectionMatrix = projectionMatrix,
            viewProjectionMatrix = viewProjectionMatrix,
        ))

        updateShaders(useMatrixPosition)
        invalid = false
    }

    private fun updateViewProjectionMatrix() {
        viewProjectionMatrix = projectionMatrix * viewMatrix
    }

    private fun updateFront(front: Vec3f) {
        this.front = front
        this.right = (front cross CAMERA_UP_VEC3).unsafe.apply { normalizeAssign() }.unsafe
        this.up = (this.right cross front).unsafe.apply { normalizeAssign() }.unsafe
    }

    private fun updateShaders(cameraPosition: Vec3f) {
        for (shader in context.system.shaders) {
            if (shader is ViewProjectionShader) {
                shader.viewProjectionMatrix = viewProjectionMatrix
            }
            if (shader is CameraPositionShader) {
                shader.cameraPosition = cameraPosition
            }
        }
    }

    fun invalidate() {
        invalid = true
    }
}
