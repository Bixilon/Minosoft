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

package de.bixilon.minosoft.gui.rendering.renderer.renderer.pipeline

import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.renderer.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererManager
import de.bixilon.minosoft.gui.rendering.renderer.renderer.pipeline.world.WorldRendererPipeline
import de.bixilon.minosoft.gui.rendering.renderer.renderer.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.PostDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.PreDrawable

class RendererPipeline(private val renderer: RendererManager) : Drawable {
    val world = WorldRendererPipeline(renderer)

    private val other: MutableList<Drawable> = mutableListOf()

    private val pre: MutableList<PreDrawable> = mutableListOf()
    private val post: MutableList<PostDrawable> = mutableListOf()

    private val renderSystem = renderer.context.system
    private val framebuffer = renderer.context.framebuffer


    private fun RenderSystem.set(renderer: Renderer) {
        val framebuffer = renderer.framebuffer
        if (framebuffer == null) {
            this.framebuffer = null
            this.polygonMode = PolygonModes.DEFAULT
        } else {
            framebuffer.bind()
        }
    }

    private fun drawOther() {
        for (renderer in other) {
            renderer as Renderer
            if (renderer.skip) continue

            renderSystem.set(renderer)
            renderer.draw()
        }
    }

    private fun drawPre() {
        for (renderer in pre) {
            if (renderer.skip) continue
            renderSystem.set(renderer)
            renderer.drawPre()
        }
    }

    private fun drawPost() {
        for (renderer in post) {
            if (renderer.skip) continue
            renderSystem.set(renderer)
            renderer.drawPost()
        }
    }


    override fun draw() {
        world.draw()
        drawOther()

        drawPre()
        framebuffer.draw()
        drawPost()
    }

    operator fun plusAssign(renderer: Renderer) {
        if (renderer is WorldRenderer) {
            return world.rebuild()
        }
        if (renderer is Drawable) {
            other += renderer
            // TODO: sort for framebuffer
        }
        if (renderer is PreDrawable) pre += renderer
        if (renderer is PostDrawable) post += renderer
    }
}
