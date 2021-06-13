/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.sky

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.modding.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.SimpleTextureMesh
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.events.TimeChangeEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.MMath
import glm_.func.rad
import glm_.mat4x4.Mat4d
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import org.lwjgl.opengl.GL11.*
import kotlin.math.cos

class SkyRenderer(
    private val connection: PlayConnection,
    val renderWindow: RenderWindow,
) : Renderer {
    private val skyboxShader = Shader(
        renderWindow = renderWindow,
        resourceLocation = ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "sky/skybox"),
    )
    private val skySunShader = Shader(
        renderWindow = renderWindow,
        resourceLocation = ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "sky/sun"),
    )
    private val skyboxMesh = SkyboxMesh()
    private var skySunMesh = SimpleTextureMesh()
    private lateinit var sunTexture: Texture
    private var recalculateSunNextFrame: Boolean = true
    var baseColor = RenderConstants.DEFAULT_SKY_COLOR


    override fun init() {
        skyboxShader.load()
        skyboxMesh.load()

        skySunShader.load()
        skySunMesh.load()


        connection.registerEvent(CallbackEventInvoker.of<CameraMatrixChangeEvent> {
            val viewProjectionMatrix = it.projectionMatrix * it.viewMatrix.toMat3().toMat4()
            renderWindow.queue += {
                skyboxShader.use().setMat4("uSkyViewProjectionMatrix", viewProjectionMatrix)
                setSunMatrix(viewProjectionMatrix)
            }
        })
        connection.registerEvent(CallbackEventInvoker.of<TimeChangeEvent> {
            if (connection.world.time != it.time) {
                recalculateSunNextFrame = true
            }
        })
        sunTexture = renderWindow.textures.allTextures.getOrPut(SUN_TEXTURE_RESOURCE_LOCATION) { Texture(SUN_TEXTURE_RESOURCE_LOCATION) }
    }

    private fun setSunMatrix(projectionViewMatrix: Mat4d) {
        val timeAngle = ((getSkyAngle(connection.world.time).toDouble() * 360.0) + 180.0).rad
        val rotatedMatrix = if (timeAngle == 0.0) {
            projectionViewMatrix
        } else {
            projectionViewMatrix.rotate(timeAngle, Vec3d(0.0f, 0.0f, 1.0f))
        }
        skySunShader.use().setMat4("uSkyViewProjectionMatrix", rotatedMatrix) // ToDo: 180° is top, not correct yet
    }

    override fun postInit() {
        renderWindow.textures.use(skySunShader)
    }

    private fun drawSun() {
        if (recalculateSunNextFrame) {
            setSunMatrix(renderWindow.inputHandler.camera.projectionMatrix * renderWindow.inputHandler.camera.viewMatrix.toMat3().toMat4())
            skySunMesh.unload()

            skySunMesh = SimpleTextureMesh()

            fun addQuad(start: Vec3, end: Vec3, texture: Texture, tintColor: RGBColor) {
                skySunMesh.addVertex(Vec3(start.x, start.y, start.z), texture, Vec2(0.0, 0.0), tintColor)
                skySunMesh.addVertex(Vec3(end.x, end.y, start.z), texture, Vec2(0.0f, 1.0), tintColor)
                skySunMesh.addVertex(Vec3(end.x, end.y, end.z), texture, Vec2(1.0f, 1.0f), tintColor)
                skySunMesh.addVertex(Vec3(end.x, end.y, end.z), texture, Vec2(1.0f, 1.0f), tintColor)
                skySunMesh.addVertex(Vec3(start.x, start.y, end.z), texture, Vec2(0.0f, 1.0f), tintColor)
                skySunMesh.addVertex(Vec3(start.x, start.y, start.z), texture, Vec2(0.0f, 0.0f), tintColor)
            }

            addQuad(
                start = Vec3(-0.15f, 1.0f, -0.15f),
                end = Vec3(+0.15f, 1.0f, +0.15f),
                texture = sunTexture,
                tintColor = RGBColor(255, 255, 255), // ToDo: Depends on time
            )
            skySunMesh.load()
        }
        skySunShader.use()
        skySunMesh.draw()
    }

    private fun checkSkyColor() {
        // ToDo: Calculate correct
        val brightness = 1.0f
        val topColor = RGBColor((baseColor.red * brightness).toInt(), (baseColor.green * brightness).toInt(), (baseColor.blue * brightness).toInt())
        val bottomColor = RGBColor(topColor.red * 8 / 9, topColor.green * 8 / 9, topColor.blue * 8 / 9)
        renderWindow.queue += {
            updateSkyColor(topColor, bottomColor)
        }
    }

    private fun updateSkyColor(topColor: RGBColor, bottomColor: RGBColor) {
        skyboxShader.use()

        skyboxShader.setRGBColor("uBottomColor", bottomColor)
        skyboxShader.setRGBColor("uTopColor", topColor)
    }

    private fun drawSkybox() {
        checkSkyColor()
        skyboxShader.use()
        skyboxMesh.draw()
    }

    override fun draw() {
        glDepthFunc(GL_LEQUAL)

        drawSkybox()
        drawSun()

        glDepthFunc(GL_LESS)
    }

    companion object : RendererBuilder<SkyRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:sky")

        private val SUN_TEXTURE_RESOURCE_LOCATION = ResourceLocation("minecraft:textures/environment/sun.png")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): SkyRenderer {
            return SkyRenderer(connection, renderWindow)
        }

        fun getSkyAngle(time: Long): Double {
            val fractionalPath = MMath.fractionalPart(time / ProtocolDefinition.TICKS_PER_DAYf - 0.25)
            val angle = 0.5 - cos(fractionalPath * Math.PI) / 2.0
            return (fractionalPath * 2.0 + angle) / 3.0
        }
    }
}
