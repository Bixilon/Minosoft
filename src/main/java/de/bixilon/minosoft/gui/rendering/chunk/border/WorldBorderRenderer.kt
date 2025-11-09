/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.border

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.math.simple.FloatMath.clamp
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor.Companion.rgba
import de.bixilon.minosoft.data.world.border.WorldBorderState
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.LayerSettings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.layer.RenderLayer
import de.bixilon.minosoft.gui.rendering.system.base.settings.RenderSettings
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStates
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import kotlin.time.Duration.Companion.seconds

class WorldBorderRenderer(
    override val context: RenderContext,
) : WorldRenderer, AsyncRenderer {
    override val layers = LayerSettings()
    private val shader = context.system.shader.create(minosoft("world/border")) { WorldBorderShader(it) }
    private var mesh: WorldBorderMesh? = null
    private val border = context.session.world.border
    private lateinit var texture: Texture
    private var offsetReset = now()
    override val skip get() = border.getDistanceTo(context.session.player.physics.position) > MAX_DISTANCE
    private var reload = false

    override fun registerLayers() {
        layers.register(WorldBorderLayer, shader, this::draw) { this.mesh == null }
    }

    override fun init(latch: AbstractLatch) {
        shader.native.defines["MAX_DISTANCE"] = MAX_DISTANCE

        texture = context.textures.static.create(TEXTURE)
        context.camera.offset::offset.observe(this) { reload = true }
    }

    override fun postInit(latch: AbstractLatch) {
        shader.load()
        context.textures.static.use(shader)
        shader.textureIndexLayer = texture.shaderId
    }

    private fun calculateColor(): RGBAColor {
        val distance = border.getDistanceTo(context.session.player.physics.position).toFloat() - 1.0f // 1 block padding
        val strength = 1.0f - distance.clamp(0.0f, MAX_DISTANCE) / MAX_DISTANCE // slowly fade in
        val color = when (border.area.state) {
            WorldBorderState.GROWING -> GROWING_COLOR
            WorldBorderState.SHRINKING -> SHRINKING_COLOR
            WorldBorderState.STATIC -> STATIC_COLOR
        }
        return color.with(alpha = (strength * strength))
    }

    private fun update() {
        if (this.mesh == null) return

        val time = now()
        if (offsetReset - time > ANIMATION_SPEED) {
            offsetReset = time
        }
        val textureOffset = ((offsetReset - time) / ANIMATION_SPEED).toFloat()
        shader.textureOffset = 1.0f - textureOffset

        shader.tint = calculateColor()
    }


    override fun prepareDrawAsync() {
        if (skip) return

        val center = border.center
        val radius = border.area.radius()

        val previous = this.mesh
        if (previous != null && !reload && center == previous.center && radius == previous.radius) return

        previous?.let { context.queue += { it.unload() } }

        val offset = context.camera.offset.offset

        val mesh = WorldBorderMeshBuilder(context, offset, center, radius)

        this.mesh = mesh.bake()
        this.reload = false
    }

    private fun draw() {
        val mesh = this.mesh ?: return
        update()

        if (mesh.state == MeshStates.PREPARING) {
            mesh.load()
        }
        mesh.draw()
    }

    private object WorldBorderLayer : RenderLayer {
        override val settings = RenderSettings(
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
        override val priority get() = 3000
    }

    companion object : RendererBuilder<WorldBorderRenderer> {
        val GROWING_COLOR = "#40FF80".rgba()
        val SHRINKING_COLOR = "#FF3030".rgba()
        val STATIC_COLOR = "#20A0FF".rgba()
        val ANIMATION_SPEED = 5.seconds
        const val MAX_DISTANCE = 100.0f

        private val TEXTURE = minecraft("misc/forcefield").texture()

        override fun build(session: PlaySession, context: RenderContext) = WorldBorderRenderer(context)
    }
}
