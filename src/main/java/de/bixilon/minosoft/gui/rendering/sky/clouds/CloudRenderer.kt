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

package de.bixilon.minosoft.gui.rendering.sky.clouds

import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.LayerSettings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.sky.SkyRenderer
import de.bixilon.minosoft.gui.rendering.sky.clouds.generator.CloudGenerator
import de.bixilon.minosoft.gui.rendering.sky.clouds.mesh.CloudStyle
import de.bixilon.minosoft.gui.rendering.system.base.layer.RenderLayer
import de.bixilon.minosoft.gui.rendering.system.base.settings.RenderSettings
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import kotlin.math.abs

class CloudRenderer(
    private val sky: SkyRenderer,
    val session: PlaySession,
    override val context: RenderContext,
) : WorldRenderer {
    override val layers = LayerSettings()
    private val color = CloudColor(sky)
    private val shader = context.system.shader.create(minosoft("sky/clouds")) { CloudShader(it) }
    var generator: CloudGenerator? = null
    var style = CloudStyle.VOLUME


    private var baseHeight: Int? = null

    private var reset = false


    override fun registerLayers() {
        layers.register(CloudRenderLayer, shader, this::draw, this::canSkip)
    }

    override fun asyncInit(latch: AbstractLatch) {
        context.camera.offset::offset.observe(this) { reset = true }
    }

    private fun canSkip(): Boolean {
        val baseHeight = baseHeight ?: return true
        if (!sky.profile.clouds.enabled) return true
        if (session.profiles.block.viewDistance < 3) return true
        if (abs(session.camera.entity.physics.position.y - baseHeight) > MAX_VERTICAL_VIEW_DISTANCE) return true

        return false
    }

    override fun postInit(latch: AbstractLatch) {
        shader.load()
        sky.profile.clouds::enabled.observe(this) { reset = true }
        sky.profile.clouds::movement.observe(this) { reset = true }
        sky.profile.clouds::generator.observe(this, instant = true) { this.generator = CloudGenerator.of(it, context.session.assets) } // TODO: flush cache
        session::state.observe(this) { reset = true }
        sky.profile.clouds::style.observe(this, instant = true) { this.style = it; reset = true }
    }

    private fun draw() {
        shader.color = color.calculate()

    }

    private object CloudRenderLayer : RenderLayer {
        override val settings = RenderSettings(faceCulling = false)
        override val priority get() = -1
    }

    companion object : RendererBuilder<CloudRenderer> {
        const val MAX_VERTICAL_VIEW_DISTANCE = 256

        override fun build(session: PlaySession, context: RenderContext): CloudRenderer? {
            val sky = context.renderer[SkyRenderer] ?: return null
            return CloudRenderer(sky, session, context)
        }
    }
}
