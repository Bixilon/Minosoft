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
import de.bixilon.minosoft.data.registries.fluid.DefaultFluids
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.RendererBuilder
import de.bixilon.minosoft.gui.rendering.modding.events.CameraMatrixChangeEvent
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.DepthFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.CustomDrawable
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.SimpleTextureMesh
import de.bixilon.minosoft.modding.event.events.TimeChangeEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.func.rad
import glm_.mat4x4.Mat4
import glm_.mat4x4.Mat4d
import glm_.vec3.Vec3
import glm_.vec3.Vec3d

class SkyRenderer(
    private val connection: PlayConnection,
    override val renderWindow: RenderWindow,
) : Renderer, CustomDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val skyboxShader = renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "sky/skybox"))
    private val skySunShader = renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "sky/sun"))
    private val skyboxMesh = SkyboxMesh(renderWindow)
    private var skySunMesh = SimpleTextureMesh(renderWindow)
    private lateinit var sunTexture: AbstractTexture
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
                skyboxShader.use().setMat4("uSkyViewProjectionMatrix", Mat4(viewProjectionMatrix))
                setSunMatrix(viewProjectionMatrix)
            }
        })
        connection.registerEvent(CallbackEventInvoker.of<TimeChangeEvent> {
            if (connection.world.time != it.time) {
                recalculateSunNextFrame = true
            }
        })
        sunTexture = renderWindow.textureManager.staticTextures.createTexture(SUN_TEXTURE_RESOURCE_LOCATION)
    }

    private fun setSunMatrix(projectionViewMatrix: Mat4d) {
        val timeAngle = (connection.world.skyAngle * 360.0).rad
        val rotatedMatrix = if (timeAngle == 0.0) {
            projectionViewMatrix
        } else {
            projectionViewMatrix.rotate(timeAngle, Vec3d(0.0f, 0.0f, 1.0f))
        }
        skySunShader.use().setMat4("uSkyViewProjectionMatrix", Mat4(rotatedMatrix))
    }

    override fun postInit() {
        renderWindow.textureManager.staticTextures.use(skySunShader)
    }

    private fun drawSun() {
        if (recalculateSunNextFrame) {
            setSunMatrix(renderWindow.inputHandler.camera.projectionMatrix * renderWindow.inputHandler.camera.viewMatrix.toMat3().toMat4())
            skySunMesh.unload()

            skySunMesh = SimpleTextureMesh(renderWindow)


            skySunMesh.addQuad(
                start = Vec3(-0.15f, 1.0f, -0.15f),
                end = Vec3(+0.15f, 1.0f, +0.15f),
                vertexConsumer = { position, textureCoordinate ->
                    skySunMesh.addVertex(
                        position = position,
                        texture = sunTexture,
                        textureCoordinates = textureCoordinate,
                        tintColor = ChatColors.WHITE.with(alpha = 1.0f - connection.world.rainGradient), // ToDo: Depends on time
                    )
                }
            )
            skySunMesh.load()
        }
        renderWindow.renderSystem.setBlendFunc(BlendingFunctions.SOURCE_ALPHA, BlendingFunctions.ONE, BlendingFunctions.ONE, BlendingFunctions.ZERO)
        skySunShader.use()
        skySunMesh.draw()
    }

    private fun checkSkyColor() {
        // ToDo: Calculate correct
        val brightness = 1.0f
        val skyColor = RGBColor((baseColor.red * brightness).toInt(), (baseColor.green * brightness).toInt(), (baseColor.blue * brightness).toInt())

        renderWindow.inputHandler.camera.fogColor.value = if (connection.player.submergedFluid?.resourceLocation == DefaultFluids.WATER) {
            connection.player.positionInfo.biome?.waterFogColor ?: skyColor
        } else {
            skyColor
        }


        for (shader in renderWindow.renderSystem.shaders) {
            if (shader.uniforms.contains("uSkyColor")) {
                shader.use().setRGBColor("uSkyColor", skyColor)
            }
        }
    }

    private fun drawSkybox() {
        checkSkyColor()
        skyboxShader.use()
        skyboxMesh.draw()
    }

    override fun drawCustom() {
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
