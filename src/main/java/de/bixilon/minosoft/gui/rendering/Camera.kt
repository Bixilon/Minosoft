package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.gui.rendering.shader.Shader
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW
import kotlin.math.cos
import kotlin.math.sin

class Camera(private var fov: Float, private val windowId: Long) {
    private var mouseSensitivity = 0.1
    private var movementSpeed = 7
    private var cameraFront = Vec3(0.0f, 0.0f, -1.0f)
    private var cameraPosition = Vec3(0.0f, 0.0f, 3.0f)
    private var lastMouseX = 0.0
    private var lastMouseY = 0.0
    private var yaw = 0.0
    private var pitch = 0.0

    fun mouseCallback(xPos: Double, yPos: Double) {
        var xOffset = xPos - this.lastMouseX
        var yOffset = this.lastMouseY - yPos // reversed since y-coordinates go from bottom to top
        lastMouseX = xPos
        lastMouseY = yPos
        xOffset *= mouseSensitivity
        yOffset *= mouseSensitivity
        yaw += xOffset
        pitch += yOffset

        // make sure that when pitch is out of bounds, screen doesn't get flipped
        if (this.pitch > 89.0) {
            this.pitch = 89.0
        } else if (this.pitch < -89.0) {
            this.pitch = -89.0
        }
        cameraFront = Vec3((cos(glm.radians(yaw)) * cos(glm.radians(pitch))).toFloat(), sin(glm.radians(pitch)).toFloat(), (sin(glm.radians(yaw)) * cos(glm.radians(pitch))).toFloat()).normalize()
    }

    fun handleInput(deltaTime: Double) {
        val cameraSpeed = movementSpeed * deltaTime
        val currentY = cameraPosition.y
        if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            cameraPosition = cameraPosition + cameraFront * cameraSpeed
        }
        if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            cameraPosition = cameraPosition - cameraFront * cameraSpeed
        }
        if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            cameraPosition = cameraPosition - (cameraFront.cross(CAMERA_UP_VEC3).normalize()) * cameraSpeed
        }
        if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            cameraPosition = cameraPosition + (cameraFront.cross(CAMERA_UP_VEC3).normalize()) * cameraSpeed
        }
        this.cameraPosition.y = currentY // stay on xz line when moving (aka. no clip)
        if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            cameraPosition = cameraPosition - CAMERA_UP_VEC3 * cameraSpeed
        }
        if (GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            cameraPosition = cameraPosition + CAMERA_UP_VEC3 * cameraSpeed
        }
    }

    fun calculateProjectionMatrix(screenWidth: Int, screenHeight: Int, shader: Shader) {
        shader.use().setMat4("projectionMatrix", calculateProjectionMatrix(screenWidth, screenHeight))
    }

    private fun calculateProjectionMatrix(screenWidth: Int, screenHeight: Int): Mat4 {
        return glm.perspective(glm.radians(fov), screenWidth.toFloat() / screenHeight.toFloat(), 0.1f, 1000f)
    }

    fun calculateViewMatrix(shader: Shader) {
        shader.use().setMat4("viewMatrix", calculateViewMatrix())
    }

    private fun calculateViewMatrix(): Mat4 {
        return glm.lookAt(cameraPosition, cameraPosition + cameraFront, CAMERA_UP_VEC3)
    }

    fun setFOV(fov: Float) {
        this.fov = fov
    }

    fun setPosition(position: Vec3) {
        cameraPosition = position
    }

    companion object {
        private val CAMERA_UP_VEC3 = Vec3(0.0f, 1.0f, 0.0f)
    }
}
