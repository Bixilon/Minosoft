package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.Location
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketPlayerPositionAndRotationSending
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec3.Vec3
import org.lwjgl.glfw.GLFW.*
import kotlin.math.cos
import kotlin.math.sin

class Camera(private val connection: Connection, private var fov: Float, private val windowId: Long) {
    private var mouseSensitivity = 0.1
    private var movementSpeed = 7
    var cameraPosition = Vec3(0.0f, 0.0f, 0.0f)
    private var lastMouseX = 0.0
    private var lastMouseY = 0.0
    var yaw = 0.0
    var pitch = 0.0
    private var zoom = 0f

    private var lastPositionChange = 0L
    private var currentPositionSent = false

    private var cameraFront = Vec3(0.0f, 0.0f, -1.0f)
    private var cameraRight = Vec3(0.0f, 0.0f, -1.0f)
    private var cameraUp = Vec3(0.0f, 1.0f, 0.0f)

    private var screenHeight = 0
    private var screenWidth = 0
    private val shaders: MutableList<Shader> = mutableListOf()

    fun mouseCallback(xPos: Double, yPos: Double) {
        var xOffset = xPos - this.lastMouseX
        var yOffset = this.lastMouseY - yPos // reversed since y-coordinates go from bottom to top
        lastMouseX = xPos
        lastMouseY = yPos
        xOffset *= mouseSensitivity
        yOffset *= mouseSensitivity
        yaw += xOffset
        pitch -= yOffset

        // make sure that when pitch is out of bounds, screen doesn't get flipped
        if (this.pitch > 89.9) {
            this.pitch = 89.9
        } else if (this.pitch < -89.9) {
            this.pitch = -89.9
        }
        if (this.yaw > 180) {
            this.yaw -= 360
        } else if (this.yaw < -180) {
            this.yaw += 360
        }
        this.yaw %= 180
        setRotation(yaw, pitch)
    }

    fun handleInput(deltaTime: Double) {
        var cameraSpeed = movementSpeed * deltaTime

        if (glfwGetKey(windowId, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) {
            cameraSpeed *= 5
        }
        val lastPosition = cameraPosition
        val currentY = cameraPosition.y
        if (glfwGetKey(windowId, GLFW_KEY_W) == GLFW_PRESS) {
            cameraPosition = cameraPosition + cameraFront * cameraSpeed
        }
        if (glfwGetKey(windowId, GLFW_KEY_S) == GLFW_PRESS) {
            cameraPosition = cameraPosition - cameraFront * cameraSpeed
        }
        if (glfwGetKey(windowId, GLFW_KEY_A) == GLFW_PRESS) {
            cameraPosition = cameraPosition - cameraRight * cameraSpeed
        }
        if (glfwGetKey(windowId, GLFW_KEY_D) == GLFW_PRESS) {
            cameraPosition = cameraPosition + cameraRight * cameraSpeed
        }
        this.cameraPosition.y = currentY // stay on xz line when moving (aka. no clip): ToDo: make movement not slower when you look up
        if (glfwGetKey(windowId, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
            cameraPosition = cameraPosition - CAMERA_UP_VEC3 * cameraSpeed
        }
        if (glfwGetKey(windowId, GLFW_KEY_SPACE) == GLFW_PRESS) {
            cameraPosition = cameraPosition + CAMERA_UP_VEC3 * cameraSpeed
        }
        if (lastPosition != cameraPosition) {
            recalculateViewMatrix()
            sendPositionToServer()
        }

        val lastZoom = zoom
        zoom = if (glfwGetKey(windowId, GLFW_KEY_C) == GLFW_PRESS) {
            2f
        } else {
            0f
        }
        if (lastZoom != zoom) {
            recalculateProjectionMatrix()
        }

    }

    fun addShaders(vararg shaders: Shader) {
        this.shaders.addAll(shaders)
    }

    fun screenChangeResizeCallback(screenWidth: Int, screenHeight: Int) {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
        recalculateProjectionMatrix()
    }

    private fun recalculateProjectionMatrix() {
        for (shader in shaders) {
            shader.use().setMat4("projectionMatrix", calculateProjectionMatrix(screenWidth, screenHeight))
        }
    }

    private fun calculateProjectionMatrix(screenWidth: Int, screenHeight: Int): Mat4 {
        return glm.perspective(glm.radians(fov / (zoom + 1.0f)), screenWidth.toFloat() / screenHeight.toFloat(), 0.1f, 1000f)
    }

    private fun recalculateViewMatrix() {
        for (shader in shaders) {
            shader.use().setMat4("viewMatrix", calculateViewMatrix())
        }
    }

    private fun calculateViewMatrix(): Mat4 {
        return glm.lookAt(cameraPosition, cameraPosition + cameraFront, CAMERA_UP_VEC3)
    }

    fun setFOV(fov: Float) {
        this.fov = fov
    }

    fun setRotation(yaw: Double, pitch: Double) {
        cameraFront = Vec3(
            (cos(glm.radians(yaw + 90)) * cos(glm.radians(-pitch))).toFloat(),
            sin(glm.radians(-pitch)).toFloat(),
            (sin(glm.radians(yaw + 90)) * cos(glm.radians(-pitch))).toFloat())
            .normalize()

        cameraRight = cameraFront.cross(CAMERA_UP_VEC3).normalize()
        cameraUp = cameraRight.cross(cameraFront).normalize()
        recalculateViewMatrix()
        sendPositionToServer()
    }

    fun draw() {
        if (!currentPositionSent) {
            sendPositionToServer()
        }
    }

    private fun sendPositionToServer() {
        if (System.currentTimeMillis() - lastPositionChange > ProtocolDefinition.TICK_TIME) {
            // ToDo: Replace this with proper movement and only send it, when our position changed
            connection.sendPacket(PacketPlayerPositionAndRotationSending(Location(cameraPosition), EntityRotation(yaw, pitch), false))
            lastPositionChange = System.currentTimeMillis()
            currentPositionSent = true
            return
        }
        currentPositionSent = false
    }

    companion object {
        private val CAMERA_UP_VEC3 = Vec3(0.0f, 1.0f, 0.0f)
    }
}
