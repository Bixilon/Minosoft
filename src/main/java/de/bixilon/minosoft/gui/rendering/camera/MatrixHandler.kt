package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.camera.frustum.Frustum
import de.bixilon.minosoft.gui.rendering.modding.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.modding.events.CameraPositionChangeEvent
import de.bixilon.minosoft.gui.rendering.modding.events.FrustumChangeEvent
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import glm_.func.rad
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class MatrixHandler(
    private val renderWindow: RenderWindow,
) {
    private val connection = renderWindow.connection
    private val profile = renderWindow.connection.profiles.rendering.camera
    val frustum = Frustum(this)
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

    var viewMatrix = calculateViewMatrix()
        private set
    var projectionMatrix = calculateProjectionMatrix(renderWindow.window.sizef)
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


    private fun calculateViewMatrix(eyePosition: Vec3 = entity.eyePosition): Mat4 {
        return glm.lookAt(eyePosition, eyePosition + cameraFront, CAMERA_UP_VEC3)
    }

    private fun calculateProjectionMatrix(screenDimensions: Vec2): Mat4 {
        return glm.perspective(fov.rad.toFloat(), screenDimensions.x / screenDimensions.y, 0.01f, 10000.0f)
    }

    fun init() {
        connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> {
            projectionMatrix = calculateProjectionMatrix(Vec2(it.size))
            upToDate = false
        })
        draw() // set initial values
    }

    fun draw() {
        val fov = fov
        val eyePosition = entity.eyePosition
        val rotation = entity.rotation
        if (upToDate && eyePosition == this.eyePosition && rotation == this.rotation && fov == previousFOV) {
            return
        }
        this.eyePosition = eyePosition
        this.rotation = rotation
        previousFOV = fov

        updateRotation(rotation)
        updateViewMatrix(eyePosition)
        updateFrustum()

        connection.fireEvent(CameraPositionChangeEvent(renderWindow, eyePosition))

        connection.fireEvent(CameraMatrixChangeEvent(
            renderWindow = renderWindow,
            viewMatrix = viewMatrix,
            projectionMatrix = projectionMatrix,
            viewProjectionMatrix = viewProjectionMatrix,
        ))

        updateShaders()
    }

    private fun updateViewMatrix(eyePosition: Vec3) {
        viewMatrix = calculateViewMatrix(eyePosition)
        viewProjectionMatrix = projectionMatrix * viewMatrix
    }

    private fun updateFrustum() {
        frustum.recalculate()
        connection.fireEvent(FrustumChangeEvent(renderWindow, frustum))
    }

    private fun updateRotation(rotation: EntityRotation = entity.rotation) {
        cameraFront = rotation.front

        cameraRight = (cameraFront cross CAMERA_UP_VEC3).normalize()
        cameraUp = (cameraRight cross cameraFront).normalize()
    }

    private fun updateShaders() {
        for (shader in renderWindow.renderSystem.shaders) {
            if (shader.uniforms.contains("uViewProjectionMatrix")) {
                shader.use().setMat4("uViewProjectionMatrix", viewProjectionMatrix)
            }
            if (shader.uniforms.contains("uCameraPosition")) {
                shader.use().setVec3("uCameraPosition", connection.player.cameraPosition)
            }
        }
    }

    companion object {
        val CAMERA_UP_VEC3 = Vec3(0.0, 1.0, 0.0)
    }
}
