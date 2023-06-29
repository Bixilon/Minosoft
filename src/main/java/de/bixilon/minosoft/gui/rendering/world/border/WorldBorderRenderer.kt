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
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asColor
import de.bixilon.minosoft.data.world.border.WorldBorderState
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.SkipAll
import de.bixilon.minosoft.gui.rendering.system.base.phases.TranslucentDrawable
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class WorldBorderRenderer(
    override val context: RenderContext,
) : Renderer, AsyncRenderer, TranslucentDrawable, SkipAll {
    override val renderSystem: RenderSystem = context.system
    private val shader = renderSystem.createShader(minosoft("world/border")) { WorldBorderShader(it) }
    private var borderMesh: WorldBorderMesh? = null
    private val border = context.connection.world.border
    private lateinit var texture: Texture
    private var offsetReset = millis()
    override val skipAll: Boolean
        get() = border.getDistanceTo(context.connection.player.physics.position) > MAX_DISTANCE
    private var reload = false

    override fun init(latch: AbstractLatch) {
        shader.native.defines["MAX_DISTANCE"] = MAX_DISTANCE
        shader.load()

        texture = context.textures.staticTextures.createTexture(TEXTURE)
        context.camera.offset::offset.observe(this) { reload = true }
    }

    override fun postInit(latch: AbstractLatch) {
        context.textures.staticTextures.use(shader)
        shader.textureIndexLayer = texture.renderData.shaderTextureId
    }

    private fun calculateColor(): RGBColor {
        val distance = border.getDistanceTo(context.connection.player.physics.position).toFloat() - 1.0f // 1 block padding
        val strength = 1.0f - distance.clamp(0.0f, MAX_DISTANCE) / MAX_DISTANCE // slowly fade in
        val color = when (border.area.state) {
            WorldBorderState.GROWING -> GROWING_COLOR
            WorldBorderState.SHRINKING -> SHRINKING_COLOR
            WorldBorderState.STATIC -> STATIC_COLOR
        }
        return color.with(alpha = (strength * strength))
    }

    private fun update() {
        if (this.borderMesh == null) return

        val time = millis()
        if (offsetReset - time > ANIMATION_SPEED) {
            offsetReset = time
        }
        val textureOffset = (offsetReset - time) / ANIMATION_SPEED.toFloat()
        shader.textureOffset = 1.0f - textureOffset

        shader.tintColor = calculateColor()
    }


    override fun prepareDrawAsync() {
        if (skipAll) return

        val center = border.center
        val radius = border.area.radius()

        val previous = this.borderMesh
        if (previous != null && !reload && center == previous.center && radius == previous.radius) return

        context.queue += { previous?.unload() }

        val offset = context.camera.offset.offset

        val mesh = WorldBorderMesh(context, offset, center, radius)
        mesh.build()

        this.borderMesh = mesh
        this.reload = false
    }

    override fun setupTranslucent() {
        val mesh = this.borderMesh ?: return
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
        update()

        if (mesh.state == Mesh.MeshStates.PREPARING) {
            mesh.load()
        }
    }

    override fun drawTranslucent() {
        borderMesh?.draw()
    }

    companion object : RendererBuilder<WorldBorderRenderer> {
        override val identifier = minosoft("world_border")
        val GROWING_COLOR = "#40FF80".asColor()
        val SHRINKING_COLOR = "#FF3030".asColor()
        val STATIC_COLOR = "#20A0FF".asColor()
        const val ANIMATION_SPEED = 2000
        const val MAX_DISTANCE = 100.0f

        private val TEXTURE = "minecraft:misc/forcefield".toResourceLocation().texture()

        override fun build(connection: PlayConnection, context: RenderContext): WorldBorderRenderer {
            return WorldBorderRenderer(context)
        }
    }
}
