/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.input.camera

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.config.game.controls.KeyBindingsNames
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.player.LocalPlayerEntity
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.modding.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.modding.events.CameraPositionChangeEvent
import de.bixilon.minosoft.gui.rendering.modding.events.FrustumChangeEvent
import de.bixilon.minosoft.gui.rendering.modding.events.ScreenResizeEvent
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.floor
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getWorldOffset
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.func.cos
import glm_.func.rad
import glm_.func.sin
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class Camera(
    val connection: PlayConnection,
    var fov: Float,
    val renderWindow: RenderWindow,
) {
    private var mouseSensitivity = Minosoft.getConfig().config.game.camera.moseSensitivity
    val entity: LocalPlayerEntity
        get() = connection.player
    private var lastMouseX = 0.0
    private var lastMouseY = 0.0
    private var zoom = 0.0f

    var cameraFront = Vec3(0.0f, 0.0f, -1.0f)
    var cameraRight = Vec3(0.0f, 0.0f, -1.0f)
    private var cameraUp = Vec3(0.0f, 1.0f, 0.0f)

    val frustum: Frustum = Frustum(this)


    var viewMatrix = calculateViewMatrix()
        private set
    var projectionMatrix = calculateProjectionMatrix(renderWindow.screenDimensionsF)
        private set
    var viewProjectionMatrix = projectionMatrix * viewMatrix
        private set
    var lastFlyKeyDown = false

    fun mouseCallback(xPos: Double, yPos: Double) {
        var xOffset = xPos - this.lastMouseX
        var yOffset = yPos - this.lastMouseY
        lastMouseX = xPos
        lastMouseY = yPos
        if (renderWindow.inputHandler.currentKeyConsumer != null) {
            return
        }
        xOffset *= mouseSensitivity
        yOffset *= mouseSensitivity
        var yaw = xOffset.toFloat() + entity.rotation.headYaw
        if (yaw > 180) {
            yaw -= 360
        } else if (yaw < -180) {
            yaw += 360
        }
        yaw %= 180
        val pitch = glm.clamp(yOffset.toFloat() + entity.rotation.pitch, -89.9f, 89.9f)
        setRotation(yaw, pitch)
    }

    fun init(renderWindow: RenderWindow) {
        renderWindow.inputHandler.registerCheckCallback(
            KeyBindingsNames.MOVE_SPRINT,
            KeyBindingsNames.MOVE_FORWARD,
            KeyBindingsNames.MOVE_BACKWARDS,
            KeyBindingsNames.MOVE_LEFT,
            KeyBindingsNames.MOVE_RIGHT,
            KeyBindingsNames.MOVE_FLY_UP,
            KeyBindingsNames.MOVE_FLY_DOWN,
            KeyBindingsNames.ZOOM,
            KeyBindingsNames.MOVE_JUMP,
            KeyBindingsNames.MOVE_SNEAK,
            KeyBindingsNames.MOVE_TOGGLE_FLY,
        )

        connection.registerEvent(CallbackEventInvoker.of<ScreenResizeEvent> { recalculateViewProjectionMatrix() })
        frustum.recalculate()
        connection.fireEvent(FrustumChangeEvent(renderWindow, frustum))
    }

    private fun recalculateViewProjectionMatrix() {
        viewMatrix = calculateViewMatrix()
        projectionMatrix = calculateProjectionMatrix(renderWindow.screenDimensionsF)
        viewProjectionMatrix = projectionMatrix * viewMatrix
        connection.fireEvent(CameraMatrixChangeEvent(
            renderWindow = renderWindow,
            viewMatrix = viewMatrix,
            projectionMatrix = projectionMatrix,
            viewProjectionMatrix = viewProjectionMatrix,
        ))
        for (shader in renderWindow.shaders) {
            if (shader.uniforms.contains("uViewProjectionMatrix")) {
                shader.use().setMat4("uViewProjectionMatrix", viewProjectionMatrix)
            }
        }
    }

    private fun onPositionChange() {
        recalculateViewProjectionMatrix()
        // recalculate sky color for current biome
        val skyRenderer = renderWindow[SkyRenderer.Companion] ?: return
        skyRenderer.baseColor = connection.world.getBiome(entity.positionInfo.blockPosition)?.skyColor ?: RenderConstants.DEFAULT_SKY_COLOR

        frustum.recalculate()
        connection.fireEvent(FrustumChangeEvent(renderWindow, frustum))

        connection.world.dimension?.hasSkyLight?.let {
            if (it) {
                skyRenderer.baseColor = entity.positionInfo.biome?.skyColor ?: RenderConstants.DEFAULT_SKY_COLOR
            } else {
                skyRenderer.baseColor = RenderConstants.BLACK_COLOR
            }
        } ?: let { skyRenderer.baseColor = RenderConstants.DEFAULT_SKY_COLOR }
        connection.fireEvent(CameraPositionChangeEvent(renderWindow, entity.eyePosition))
    }

    private fun calculateProjectionMatrix(screenDimensions: Vec2): Mat4 {
        return glm.perspective((fov / (zoom + 1.0f)).rad, screenDimensions.x / screenDimensions.y, 0.1f, 1000f)
    }

    private fun calculateViewMatrix(): Mat4 {
        val cameraPosition = entity.eyePosition
        return glm.lookAt(cameraPosition, cameraPosition + cameraFront, CAMERA_UP_VEC3)
    }

    fun setRotation(yaw: Float, pitch: Float) {
        entity.rotation = EntityRotation(yaw.toDouble(), pitch.toDouble())

        cameraFront = Vec3(
            (yaw + 90).rad.cos * (-pitch).rad.cos,
            (-pitch).rad.sin,
            (yaw + 90).rad.sin * (-pitch).rad.cos
        ).normalize()

        cameraRight = (cameraFront cross CAMERA_UP_VEC3).normalize()
        cameraUp = (cameraRight cross cameraFront).normalize()
        recalculateViewProjectionMatrix()
    }

    fun draw() {
        val input = if (renderWindow.inputHandler.currentKeyConsumer == null) {
            MovementInput(
                pressingForward = renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.MOVE_FORWARD),
                pressingBack = renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.MOVE_BACKWARDS),
                pressingLeft = renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.MOVE_LEFT),
                pressingRight = renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.MOVE_RIGHT),
                jumping = renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.MOVE_JUMP),
                sneaking = renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.MOVE_SNEAK),
                sprinting = renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.MOVE_SPRINT),
                flyDown = renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.MOVE_FLY_DOWN),
                flyUp = renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.MOVE_FLY_UP),
                toggleFlyDown = renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.MOVE_TOGGLE_FLY),
            )
        } else {
            MovementInput()
        }
        entity.input = input
        entity.tick() // The thread pool might be busy, we force a tick here to avoid lagging

        zoom = if (renderWindow.inputHandler.isKeyBindingDown(KeyBindingsNames.ZOOM)) {
            2f
        } else {
            0.0f
        }
        // ToDo: Only update if changed
        onPositionChange()
    }

    fun getTargetBlock(): RaycastHit? {
        return raycast(entity.eyePosition, cameraFront)
    }

    private fun raycast(origin: Vec3, direction: Vec3): RaycastHit? {
        val currentPosition = Vec3(origin)

        fun getTotalDistance(): Float {
            return (origin - currentPosition).length()
        }

        for (i in 0..RAYCAST_MAX_STEPS) {
            val blockPosition = currentPosition.floor
            val blockState = connection.world.getBlockState(blockPosition)
            if (blockState != null) {
                val voxelShapeRaycastResult = (blockState.outlineShape + blockPosition + blockPosition.getWorldOffset(blockState.block)).raycast(currentPosition, direction)
                if (voxelShapeRaycastResult.hit) {
                    currentPosition += direction * voxelShapeRaycastResult.distance
                    return RaycastHit(
                        currentPosition,
                        blockPosition,
                        getTotalDistance(),
                        blockState,
                        voxelShapeRaycastResult.direction,
                        i,
                    )
                }
            }
            currentPosition += direction * (VecUtil.getDistanceToNextIntegerAxisInDirection(currentPosition, direction) + 0.001)
        }
        return null
    }

    companion object {
        val CAMERA_UP_VEC3 = Vec3(0.0f, 1.0f, 0.0f)

        private const val RAYCAST_MAX_STEPS = 100
    }
}
