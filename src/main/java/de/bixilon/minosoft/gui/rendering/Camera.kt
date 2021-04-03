/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.config.key.KeyAction
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.player.PlayerEntity
import de.bixilon.minosoft.data.mappings.biomes.Biome
import de.bixilon.minosoft.gui.rendering.chunk.Frustum
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.blockPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkSectionPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketPlayerPositionAndRotationSending
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketPlayerPositionSending
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketPlayerRotationSending
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.func.cos
import glm_.func.rad
import glm_.func.sin
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class Camera(
    val connection: Connection,
    var fov: Float,
    val renderWindow: RenderWindow,
) {
    private var mouseSensitivity = Minosoft.getConfig().config.game.camera.moseSensitivity
    private var movementSpeed = 7
    var cameraPosition = Vec3(0.0f, 0.0f, 0.0f)
    private var lastMouseX = 0.0
    private var lastMouseY = 0.0
    val playerEntity: PlayerEntity = connection.player.entity
    var yaw = 0.0
    var pitch = 0.0
    private var zoom = 0.0f

    private var lastMovementPacketSent = 0L
    private var currentPositionSent = false
    private var currentRotationSent = false

    var cameraFront = Vec3(0.0f, 0.0f, -1.0f)
    var cameraRight = Vec3(0.0f, 0.0f, -1.0f)
    private var cameraUp = Vec3(0.0f, 1.0f, 0.0f)

    var blockPosition: Vec3i = Vec3i(0, 0, 0)
        private set
    var currentBiome: Biome? = null
        private set
    var chunkPosition: Vec2i = Vec2i(0, 0)
        private set
    var sectionHeight: Int = 0
        private set
    var inChunkSectionPosition: Vec3i = Vec3i(0, 0, 0)
        private set

    val frustum: Frustum = Frustum(this)

    private val shaders: MutableSet<Shader> = mutableSetOf()

    private var keyForwardDown = false
    private var keyLeftDown = false
    private var keyRightDown = false
    private var keyBackDown = false
    private var keyFlyUp = false
    private var keyFlyDown = false
    private var keySprintDown = false
    private var keyZoomDown = false

    fun mouseCallback(xPos: Double, yPos: Double) {
        var xOffset = xPos - this.lastMouseX
        var yOffset = yPos - this.lastMouseY
        lastMouseX = xPos
        lastMouseY = yPos
        xOffset *= mouseSensitivity
        yOffset *= mouseSensitivity
        var yaw = xOffset.toFloat() + playerEntity.rotation.headYaw
        var pitch = yOffset.toFloat() + playerEntity.rotation.pitch

        // make sure that when pitch is out of bounds, screen doesn't get flipped
        if (pitch > 89.9) {
            pitch = 89.9f
        } else if (pitch < -89.9) {
            pitch = -89.9f
        }
        if (yaw > 180) {
            yaw -= 360
        } else if (yaw < -180) {
            yaw += 360
        }
        yaw %= 180
        setRotation(yaw, pitch)
    }

    fun init(renderWindow: RenderWindow) {
        renderWindow.registerKeyCallback(KeyBindingsNames.MOVE_FORWARD) { _: KeyCodes, keyAction: KeyAction ->
            keyForwardDown = keyAction == KeyAction.PRESS
        }
        renderWindow.registerKeyCallback(KeyBindingsNames.MOVE_LEFT) { _: KeyCodes, keyAction: KeyAction ->
            keyLeftDown = keyAction == KeyAction.PRESS
        }
        renderWindow.registerKeyCallback(KeyBindingsNames.MOVE_BACKWARDS) { _: KeyCodes, keyAction: KeyAction ->
            keyBackDown = keyAction == KeyAction.PRESS
        }
        renderWindow.registerKeyCallback(KeyBindingsNames.MOVE_RIGHT) { _: KeyCodes, keyAction: KeyAction ->
            keyRightDown = keyAction == KeyAction.PRESS
        }
        renderWindow.registerKeyCallback(KeyBindingsNames.MOVE_FLY_UP) { _: KeyCodes, keyAction: KeyAction ->
            keyFlyUp = keyAction == KeyAction.PRESS
        }
        renderWindow.registerKeyCallback(KeyBindingsNames.MOVE_FLY_DOWN) { _: KeyCodes, keyAction: KeyAction ->
            keyFlyDown = keyAction == KeyAction.PRESS
        }
        renderWindow.registerKeyCallback(KeyBindingsNames.MOVE_SPRINT) { _: KeyCodes, keyAction: KeyAction ->
            keySprintDown = keyAction == KeyAction.PRESS
        }
        renderWindow.registerKeyCallback(KeyBindingsNames.ZOOM) { _: KeyCodes, keyAction: KeyAction ->
            keyZoomDown = keyAction == KeyAction.PRESS
        }
    }

    fun handleInput(deltaTime: Double) {
        var cameraSpeed = movementSpeed * deltaTime
        val movementFront = Vec3(cameraFront)
        movementFront.y = 0.0f
        movementFront.normalizeAssign() // when moving forwards, do not move down
        if (keySprintDown) {
            cameraSpeed *= 5
        }
        var deltaMovement = Vec3()
        if (keyForwardDown) {
            deltaMovement = deltaMovement + movementFront * cameraSpeed
        }
        if (keyBackDown) {
            deltaMovement = deltaMovement - movementFront * cameraSpeed
        }
        if (keyLeftDown) {
            deltaMovement = deltaMovement - cameraRight * cameraSpeed
        }
        if (keyRightDown) {
            deltaMovement = deltaMovement + cameraRight * cameraSpeed
        }
        if (keyFlyDown) {
            deltaMovement = deltaMovement - CAMERA_UP_VEC3 * cameraSpeed
        }
        if (keyFlyUp) {
            deltaMovement = deltaMovement + CAMERA_UP_VEC3 * cameraSpeed
        }
        if (deltaMovement != VecUtil.EMPTY_VEC3) {
            playerEntity.move(deltaMovement)
            recalculateViewProjectionMatrix()
            currentPositionSent = false
            sendPositionToServer()
        }

        val lastZoom = zoom
        zoom = if (keyZoomDown) {
            2f
        } else {
            0.0f
        }
        if (lastZoom != zoom) {
            recalculateViewProjectionMatrix()
        }
    }

    fun addShaders(vararg shaders: Shader) {
        this.shaders.addAll(shaders)
    }

    fun screenChangeResizeCallback() {
        recalculateViewProjectionMatrix()
    }

    private fun recalculateViewProjectionMatrix() {
        val matrix = calculateProjectionMatrix(renderWindow.screenDimensionsF) * calculateViewMatrix()
        for (shader in shaders) {
            shader.use().setMat4("viewProjectionMatrix", matrix)
        }

        positionChangeCallback()
    }

    private fun positionChangeCallback() {
        blockPosition = (cameraPosition - Vec3(0, PLAYER_HEIGHT, 0)).blockPosition
        currentBiome = connection.world.getBiome(blockPosition)
        chunkPosition = blockPosition.chunkPosition
        sectionHeight = blockPosition.sectionHeight
        inChunkSectionPosition = blockPosition.inChunkSectionPosition
        // recalculate sky color for current biome
        renderWindow.setSkyColor(connection.world.getBiome(blockPosition)?.skyColor ?: RenderConstants.DEFAULT_SKY_COLOR)

        frustum.recalculate()
        renderWindow.worldRenderer.recalculateVisibleChunks()

        connection.world.dimension?.hasSkyLight?.let {
            if (it) {
                renderWindow.setSkyColor(currentBiome?.skyColor ?: RenderConstants.DEFAULT_SKY_COLOR)
            } else {
                renderWindow.setSkyColor(RenderConstants.BLACK_COLOR)
            }
        } ?: renderWindow.setSkyColor(RenderConstants.DEFAULT_SKY_COLOR)
    }

    private fun calculateProjectionMatrix(screenDimensions: Vec2): Mat4 {
        return glm.perspective((fov / (zoom + 1.0f)).rad, screenDimensions.x / screenDimensions.y, 0.1f, 1000f)
    }

    private fun calculateViewMatrix(): Mat4 {
        cameraPosition = playerEntity.position + Vec3(0, PLAYER_HEIGHT, 0)
        return glm.lookAt(cameraPosition, cameraPosition + cameraFront, CAMERA_UP_VEC3)
    }

    fun setFOV(fov: Float) {
        this.fov = fov
    }

    fun setRotation(yaw: Float, pitch: Float) {
        playerEntity.rotation = EntityRotation(yaw.toDouble(), pitch.toDouble())

        cameraFront = Vec3(
            (yaw + 90).rad.cos * (-pitch).rad.cos,
            (-pitch).rad.sin,
            (yaw + 90).rad.sin * (-pitch).rad.cos
        ).normalize()

        cameraRight = (cameraFront cross CAMERA_UP_VEC3).normalize()
        cameraUp = (cameraRight cross cameraFront).normalize()
        recalculateViewProjectionMatrix()
        currentRotationSent = false
        sendPositionToServer()
    }

    fun draw() {
        if (!currentPositionSent || !currentRotationSent) {
            sendPositionToServer()
        }
    }

    private fun sendPositionToServer() {
        if (System.currentTimeMillis() - lastMovementPacketSent > ProtocolDefinition.TICK_TIME) {
            if (!currentPositionSent && !currentPositionSent) {
                connection.sendPacket(PacketPlayerPositionAndRotationSending(playerEntity.position, playerEntity.rotation, false))
            } else if (!currentPositionSent) {
                connection.sendPacket(PacketPlayerPositionSending(playerEntity.position, false))
            } else {
                connection.sendPacket(PacketPlayerRotationSending(playerEntity.rotation, false))
            }
            lastMovementPacketSent = System.currentTimeMillis()
            currentPositionSent = true
            currentRotationSent = true
            return
        }
        if (System.currentTimeMillis() - lastMovementPacketSent < ProtocolDefinition.TICK_TIME) {
            return
        }

        if (!currentPositionSent && !currentPositionSent) {
            connection.sendPacket(PacketPlayerPositionAndRotationSending(cameraPosition - Vec3(0, PLAYER_HEIGHT, 0), EntityRotation(yaw, pitch), false))
        } else if (!currentPositionSent) {
            connection.sendPacket(PacketPlayerPositionSending(cameraPosition - Vec3(0, PLAYER_HEIGHT, 0), false))
        } else {
            connection.sendPacket(PacketPlayerRotationSending(EntityRotation(yaw, pitch), false))
        }
        lastMovementPacketSent = System.currentTimeMillis()
        currentPositionSent = true
        currentRotationSent = true
    }

    fun setPosition(position: Vec3) {
        cameraPosition = (position + Vec3(0, PLAYER_HEIGHT, 0))
        playerEntity.position = position
    }

    companion object {
        private val CAMERA_UP_VEC3 = Vec3(0.0f, 1.0f, 0.0f)
        private const val PLAYER_HEIGHT = 1.3 // player is 1.8 blocks high, the camera is normally at 0.5. 1.8 - 0.5 = 1.13
        const val PLAYER_WIDTH = 0.60001
    }
}
