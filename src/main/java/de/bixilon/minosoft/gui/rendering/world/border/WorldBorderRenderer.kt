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

package de.bixilon.minosoft.gui.rendering.world.border

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor.Companion.asColor
import de.bixilon.minosoft.data.world.border.WorldBorderState
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class WorldBorderRenderer(
    override val renderWindow: RenderWindow,
) : Renderer, TranslucentDrawable {
    override val renderSystem: RenderSystem = renderWindow.renderSystem
    private val shader = renderSystem.createShader(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "world/border"))
    private var borderMesh = WorldBorderMesh(renderWindow)
    private val border = renderWindow.connection.world.border
    private lateinit var texture: AbstractTexture
    private var offsetReset = TimeUtil.millis

    override fun init(latch: CountUpAndDownLatch) {
        shader.load()
        borderMesh.load()

        texture = renderWindow.textureManager.staticTextures.createTexture(TEXTURE)
    }

    override fun postInit(latch: CountUpAndDownLatch) {
        renderWindow.textureManager.staticTextures.use(shader)
        shader.setUInt("uIndexLayer", texture.renderData.shaderTextureId)
    }

    override fun setupTranslucent() {
        renderSystem.reset(
            blending = true,
            sourceRGB = BlendingFunctions.SOURCE_ALPHA,
            destinationRGB = BlendingFunctions.ONE,
            sourceAlpha = BlendingFunctions.SOURCE_ALPHA,
            destinationAlpha = BlendingFunctions.DESTINATION_ALPHA,
            faceCulling = false,
            polygonOffset = true,
            polygonOffsetFactor = -3.0f,
            polygonOffsetUnit = -3.0f,
        )
        shader.use()
        val time = TimeUtil.millis
        if (offsetReset - time > ANIMATION_SPEED) {
            offsetReset = time
        }
        val textureOffset = (offsetReset - time) / ANIMATION_SPEED.toFloat()
        shader.setFloat("uTextureOffset", 1.0f - textureOffset)
        shader.setFloat("uCameraHeight", renderWindow.camera.matrixHandler.eyePosition.y)

        val color = when (border.state) {
            WorldBorderState.GROWING -> GROWING_COLOR
            WorldBorderState.SHRINKING -> SHRINKING_COLOR
            WorldBorderState.STATIC -> STATIC_COLOR
        }
        shader.setRGBColor("uTintColor", color)
        shader.setFloat("uRadius", border.diameter.toFloat() / 2.0f)
        shader.setVec2("uCenter", Vec2(border.center))
    }

    override fun drawTranslucent() {
        borderMesh.draw()
        borderMesh.unload()
        borderMesh = WorldBorderMesh(renderWindow)
        borderMesh.load()
    }

    companion object : RendererBuilder<WorldBorderRenderer> {
        override val RESOURCE_LOCATION = ResourceLocation("minosoft:world_border")
        val GROWING_COLOR = "#40FF80".asColor()
        val SHRINKING_COLOR = "#FF3030".asColor()
        val STATIC_COLOR = "#20A0FF".asColor()
        const val ANIMATION_SPEED = 2000

        private val TEXTURE = "minecraft:misc/forcefield".toResourceLocation().texture()

        override fun build(connection: PlayConnection, renderWindow: RenderWindow): WorldBorderRenderer {
            return WorldBorderRenderer(renderWindow)
        }
    }
}
