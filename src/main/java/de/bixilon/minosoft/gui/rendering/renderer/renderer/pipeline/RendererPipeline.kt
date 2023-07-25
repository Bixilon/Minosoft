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

package de.bixilon.minosoft.gui.rendering.renderer.renderer.pipeline

import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.renderer.renderer.Renderer
import de.bixilon.minosoft.gui.rendering.renderer.renderer.RendererManager
import de.bixilon.minosoft.gui.rendering.renderer.renderer.pipeline.world.WorldRendererPipeline
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystem
import de.bixilon.minosoft.gui.rendering.system.base.phases.PostDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.PreDrawable
import de.bixilon.minosoft.gui.rendering.system.base.phases.SkipAll

class RendererPipeline(private val renderer: RendererManager) : Drawable {
    private val world = WorldRendererPipeline(renderer)

    private val rest: MutableList<Renderer> = mutableListOf()

    private val pre: MutableList<PreDrawable> = mutableListOf()
    private val post: MutableList<PostDrawable> = mutableListOf()

    private val renderSystem = renderer.context.system
    private val framebuffer = renderer.context.framebuffer


    private fun RenderSystem.set(renderer: Renderer) {
        val framebuffer = renderer.framebuffer
        this.framebuffer = framebuffer?.framebuffer
        this.polygonMode = framebuffer?.polygonMode ?: PolygonModes.DEFAULT
    }

    private fun drawRest() {
        for (renderer in rest) {
            if (renderer !is Drawable) continue
            if (renderer.skipDraw) continue
            if (renderer is SkipAll && renderer.skipAll) continue

            renderer.draw()
        }
    }

    private fun drawPre() {
        for (renderer in pre) {
            if (renderer is SkipAll && renderer.skipAll) continue
            if (renderer.skipPre) continue
            renderSystem.set(renderer)
            renderer.drawPre()
        }
    }

    private fun drawPost() {
        for (renderer in post) {
            if (renderer is SkipAll && renderer.skipAll) continue
            if (renderer.skipPost) continue
            renderSystem.set(renderer)
            renderer.drawPost()
        }
    }


    override fun draw() {
        world.draw()
        drawRest()

        drawPre()
        framebuffer.draw()
        drawPost()
    }

}
