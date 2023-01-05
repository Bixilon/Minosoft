/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asColor
import de.bixilon.minosoft.data.world.border.WorldBorderState
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.renderer.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.minosoft
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class WorldBorderRenderer(
    override val context: RenderContext,
) : Renderer, TranslucentDrawable {
    override val renderSystem: RenderSystem = context.renderSystem
    private val shader = renderSystem.createShader(minosoft("world/border")) { WorldBorderShader(it) }
    private val borderMesh = WorldBorderMesh(context)
    private val border = context.connection.world.border
    private lateinit var texture: AbstractTexture
    private var offsetReset = millis()
    override val skipTranslucent: Boolean
        get() = border.getDistanceTo(context.connection.player.position) > MAX_DISTANCE

    override fun init(latch: CountUpAndDownLatch) {
        shader.load()
        borderMesh.load()

        texture = context.textureManager.staticTextures.createTexture(TEXTURE)
    }

    override fun postInit(latch: CountUpAndDownLatch) {
        context.textureManager.staticTextures.use(shader)
        shader.textureIndexLayer = texture.renderData.shaderTextureId
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
        val time = millis()
        if (offsetReset - time > ANIMATION_SPEED) {
            offsetReset = time
        }
        val textureOffset = (offsetReset - time) / ANIMATION_SPEED.toFloat()
        shader.textureOffset = 1.0f - textureOffset
        shader.cameraHeight = context.camera.matrixHandler.entity.eyePosition.y

        val distance = border.getDistanceTo(context.connection.player.position)
        val strength = 1.0f - (distance.toFloat().clamp(0.0f, 100.0f) / 100.0f)
        var color = when (border.state) {
            WorldBorderState.GROWING -> GROWING_COLOR
            WorldBorderState.SHRINKING -> SHRINKING_COLOR
            WorldBorderState.STATIC -> STATIC_COLOR
        }
        color = color.with(alpha = (strength * strength))
        shader.tintColor = color
        shader.radius = border.diameter.toFloat() / 2.0f
        shader.center = Vec2(border.center)
    }

    override fun drawTranslucent() {
        borderMesh.draw()
    }

    companion object : RendererBuilder<WorldBorderRenderer> {
        override val identifier = minosoft("world_border")
        val GROWING_COLOR = "#40FF80".asColor()
        val SHRINKING_COLOR = "#FF3030".asColor()
        val STATIC_COLOR = "#20A0FF".asColor()
        const val ANIMATION_SPEED = 2000
        const val MAX_DISTANCE = 1000

        private val TEXTURE = "minecraft:misc/forcefield".toResourceLocation().texture()

        override fun build(connection: PlayConnection, context: RenderContext): WorldBorderRenderer {
            return WorldBorderRenderer(context)
        }
    }
}
