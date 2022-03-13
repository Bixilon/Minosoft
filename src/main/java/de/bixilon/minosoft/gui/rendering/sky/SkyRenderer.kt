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

package de.bixilon.minosoft.gui.rendering.sky

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.modding.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.*
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.phases.PreDrawable
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.SimpleTextureMesh
import de.bixilon.minosoft.modding.event.events.TimeChangeEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.func.rad
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3

class SkyRenderer(
    private val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, PreDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val skyboxShader = renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "sky/skybox"))
    private val skySunShader = renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "sky/sun"))
    private val skyboxMesh = SkyboxMesh(renderWindow)
    private var skySunMesh = SimpleTextureMesh(renderWindow)
    private lateinit var sunTexture: AbstractTexture
    private var updateSun: Boolean = true
    private var baseColor = RenderConstants.DEFAULT_SKY_COLOR
    override val framebuffer: Framebuffer? = null
    override val polygonMode: PolygonModes = PolygonModes.DEFAULT
    private val fogManager = renderWindow.camera.fogManager

    override fun init(latch: CountUpAndDownLatch) {
        skyboxShader.load()
        skyboxMesh.load()

        skySunShader.load()
        skySunMesh.load()


        connection.registerEvent(CallbackEventInvoker.of<CameraMatrixChangeEvent> {
            val viewProjectionMatrix = it.projectionMatrix * it.viewMatrix.toMat3().toMat4()
            renderWindow.queue += {
                skyboxShader.use().setMat4("uSkyViewProjectionMatrix", Mat4(viewProjectionMatrix))
                setSunMatrix(viewProjectionMatrix)
            }
        })
        connection.registerEvent(CallbackEventInvoker.of<TimeChangeEvent> {
            if (connection.world.time.time != it.time) {
                updateSun = true
            }
        })
        sunTexture = renderWindow.textureManager.staticTextures.createTexture(SUN_TEXTURE_RESOURCE_LOCATION)
    }

    private fun setSunMatrix(projectionViewMatrix: Mat4) {
        val timeAngle = (connection.world.time.skyAngle * 360.0f).rad
        val rotatedMatrix = if (timeAngle == 0.0f) {
            projectionViewMatrix
        } else {
            projectionViewMatrix.rotate(timeAngle, Vec3(0.0f, 0.0f, 1.0f))
        }
        skySunShader.use().setMat4("uSkyViewProjectionMatrix", rotatedMatrix)
    }

    override fun postInit(latch: CountUpAndDownLatch) {
        renderWindow.textureManager.staticTextures.use(skySunShader)
    }

    private fun drawSun() {
        if (updateSun) {
            setSunMatrix(renderWindow.camera.matrixHandler.projectionMatrix * renderWindow.camera.matrixHandler.viewMatrix.toMat3().toMat4())
            skySunMesh.unload()

            skySunMesh = SimpleTextureMesh(renderWindow)
            skySunMesh.addYQuad(
                start = Vec2(-0.15f, -0.15f),
                y = 1.0f,
                end = Vec2(+0.15f, +0.15f),
                vertexConsumer = { position, uv ->
                    skySunMesh.addVertex(
                        position = position,
                        texture = sunTexture,
                        uv = uv,
                        tintColor = ChatColors.WHITE.with(alpha = 1.0f - connection.world.weather.rainGradient), // ToDo: Depends on time
                    )
                }
            )
            skySunMesh.load()
            updateSun = false
        }
        renderSystem.enable(RenderingCapabilities.BLENDING)
        renderSystem.setBlendFunction(BlendingFunctions.SOURCE_ALPHA, BlendingFunctions.ONE, BlendingFunctions.ONE, BlendingFunctions.ZERO)
        skySunShader.use()
        skySunMesh.draw()
    }

    private fun checkSkyColor() {
        // ToDo: Calculate correct
        val brightness = 1.0f
        var skyColor = RGBColor((baseColor.red * brightness).toInt(), (baseColor.green * brightness).toInt(), (baseColor.blue * brightness).toInt())

        baseColor = connection.world.getBiome(connection.player.physics.positioning.blockPosition)?.skyColor ?: RenderConstants.DEFAULT_SKY_COLOR

        connection.world.dimension?.hasSkyLight?.let {
            baseColor = if (it) {
                connection.player.currentBiome?.skyColor ?: RenderConstants.DEFAULT_SKY_COLOR
            } else {
                RenderConstants.BLACK_COLOR
            }
        } ?: let { baseColor = RenderConstants.DEFAULT_SKY_COLOR }

        fogManager.fogColor?.let { skyColor = it }

        skyboxShader.use().setRGBColor("uSkyColor", skyColor)
    }

    private fun drawSkybox() {
        checkSkyColor()
        skyboxShader.use()
        skyboxMesh.draw()
    }

    override fun drawPre() {
        renderWindow.renderSystem.reset(depth = DepthFunctions.LESS_OR_EQUAL)
        drawSkybox()
        drawSun()
    }

    companion object : RendererBuilder<SkyRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:sky")

        private val SUN_TEXTURE_RESOURCE_LOCATION = ResourceLocation("minecraft:textures/environment/sun.png")

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): SkyRenderer {
            return SkyRenderer(connection, renderWindow)
        }
    }
}
