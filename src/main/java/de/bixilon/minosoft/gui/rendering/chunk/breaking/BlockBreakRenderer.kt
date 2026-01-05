/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.breaking

import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererBuilder
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.LayerSettings
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.system.base.layer.RenderLayer
import de.bixilon.minosoft.gui.rendering.system.base.settings.RenderSettings
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class BlockBreakRenderer(
    val session: PlaySession,
    override val context: RenderContext,
    val animation: BreakAnimation,
) : WorldRenderer {
    override val layers = LayerSettings()


    override fun registerLayers() {
        //    layers.register(BlockDestroyLayer, context.shaders.genericColorShader, this::draw) { this.mesh == null }
    }

    private object BlockDestroyLayer : RenderLayer {
        override val settings = RenderSettings(
            polygonOffset = true,
            polygonOffsetFactor = -3.0f,
            polygonOffsetUnit = -3.0f,
        )
        override val priority get() = 1600
    }

    companion object : RendererBuilder<BlockBreakRenderer> {

        override fun build(session: PlaySession, context: RenderContext): BlockBreakRenderer? {
            val animation = ignoreAll { BreakAnimation.load(context.textures, context.session.assets) } ?: return null

            return BlockBreakRenderer(session, context, animation)
        }
    }
}
