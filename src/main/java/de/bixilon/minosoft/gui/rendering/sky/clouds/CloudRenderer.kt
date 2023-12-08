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

package de.bixilon.minosoft.gui.rendering.sky.clouds

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.renderer.renderer.AsyncRenderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.LayerSettings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.layer.RenderLayer
import de.bixilon.minosoft.gui.rendering.system.base.settings.RenderSettings
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import kotlin.math.pow

class CloudRenderer(
    private val sky: SkyRenderer,
    val connection: PlayConnection,
    override val context: RenderContext,
) : WorldRenderer, AsyncRenderer {
    private val color = CloudColor(sky)
    override val layers = LayerSettings()
    override val renderSystem: RenderSystem = context.system
    val shader = renderSystem.createShader(minosoft("sky/clouds")) { CloudShader(it) }
    val matrix = CloudMatrix()
    private val cloudLayers: MutableList<CloudLayer> = mutableListOf()
    private var position = Vec2i(Int.MIN_VALUE)
    private var maxDistance = 0.0f
    private var baseHeight = 0
    private var nextLayers = 0
    var flat: Boolean = false
        private set
    private var toUnload: MutableSet<CloudLayer> = mutableSetOf()

    private var time = millis()
    var delta = 0.0f
        private set

    private var reset = false


    override fun registerLayers() {
        layers.register(CloudRenderLayer, shader, this::draw, this::canSkip)
    }

    override fun asyncInit(latch: AbstractLatch) {
        matrix.load(connection.assetsManager)

        context.camera.offset::offset.observe(this) { reset() }
    }

    private fun canSkip(): Boolean {
        if (!sky.effects.clouds) return true
        if (!sky.profile.clouds.enabled) return true
        if (cloudLayers.isEmpty()) return true
        if (connection.profiles.block.viewDistance < 3) return true
        if ((connection.camera.entity.physics.position.y + 10) < connection.world.dimension.minY) return true

        return false
    }

    private fun getCloudHeight(index: Int): IntRange {
        val base = sky.effects.getCloudHeight(connection)
        this.baseHeight = base.first
        val cloudHeight = base.last - base.first

        return IntRange(baseHeight + index * cloudHeight, baseHeight + (index + 1) * cloudHeight)
    }

    override fun postInit(latch: AbstractLatch) {
        shader.load()
        sky.profile.clouds::movement.observe(this, instant = true) {
            for (layer in cloudLayers) {
                layer.movement = it
            }
        }
        sky.profile.clouds::maxDistance.observe(this, instant = true) { this.maxDistance = it }
        connection::state.observe(this) {
            if (it == PlayConnectionStates.SPAWNING) {
                if (!sky.effects.clouds) {
                    return@observe
                }
                // reset clouds
                position = Vec2i(Int.MIN_VALUE)
                for ((index, layer) in this.cloudLayers.withIndex()) {
                    layer.height = getCloudHeight(index)
                }
            }
        }
        sky.profile.clouds::layers.observe(this, instant = true) { this.nextLayers = it }
        sky.profile.clouds::flat.observe(this, instant = true) { this.flat = it }
    }

    private fun reset() {
        reset = true
    }

    private fun updateLayers(layers: Int) {
        while (layers < this.cloudLayers.size) {
            toUnload += this.cloudLayers.removeLast()
        }
        for (index in this.cloudLayers.size until layers) {
            val layer = CloudLayer(sky, this, index, getCloudHeight(index))
            this.cloudLayers += layer
        }
    }

    override fun prepareDrawAsync() {
        if (!sky.effects.clouds) {
            return
        }
        if (reset) {
            updateLayers(0)
            updateLayers(nextLayers)
            reset = false
        }
        if (cloudLayers.size != nextLayers) {
            updateLayers(nextLayers)
        }

        val time = millis()
        val delta = time - this.time
        this.delta = delta / 1000.0f
        this.time = time

        for (layer in cloudLayers) {
            layer.prepareAsync()
        }
    }

    override fun postPrepareDraw() {
        for (unload in toUnload) {
            unload.unload()
        }
        if (!sky.effects.clouds) {
            return
        }
        for (layer in cloudLayers) {
            layer.prepare()
        }
    }


    private fun setYOffset() {
        val y = (context.connection.camera.entity.renderInfo.eyePosition.y - context.camera.offset.offset.y).toFloat()
        var yOffset = 0.0f
        val over = (baseHeight - y)
        if (over > 0.0f) {
            yOffset = -(over / 15.0f).pow(2)
        }
        shader.yOffset = yOffset
    }

    private fun draw() {
        shader.cloudsColor = color.calculate()
        setYOffset()


        for (layer in cloudLayers) {
            layer.draw()
        }
    }

    private object CloudRenderLayer : RenderLayer {
        override val settings = RenderSettings(faceCulling = false)
        override val priority get() = -1
    }

    companion object : RendererBuilder<CloudRenderer> {

        override fun build(connection: PlayConnection, context: RenderContext): CloudRenderer? {
            val sky = context.renderer[SkyRenderer] ?: return null
            return CloudRenderer(sky, connection, context)
        }
    }
}
