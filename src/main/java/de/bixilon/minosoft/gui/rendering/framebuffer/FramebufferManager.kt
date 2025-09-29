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

package de.bixilon.minosoft.gui.rendering.framebuffer

import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.gui.GUIFramebuffer
import de.bixilon.minosoft.gui.rendering.framebuffer.world.WorldFramebuffer
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.util.delegate.RenderingDelegate.observeRendering

class FramebufferManager(
    private val context: RenderContext,
) : Drawable {
    val world = WorldFramebuffer(context)
    val gui = GUIFramebuffer(context)


    fun init() {
        world.init()
        gui.init()

        context.window::size.observeRendering(this, true) {
            world.resize(it)
            gui.resize(it)
        }
    }

    fun postInit() {
        world.postInit()
        gui.postInit()
    }


    fun clear() {
        world.clear()
        gui.clear()
    }


    override fun draw() {
        context.system.framebuffer = null
        context.system.polygonMode = PolygonModes.FILL
        world.draw()
        gui.draw()
    }
}
