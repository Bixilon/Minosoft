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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay

import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.DefaultOverlays
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable

class OverlayManager(
    private val context: RenderContext,
) : Drawable {
    private val overlays: MutableList<Overlay> = mutableListOf()

    fun init() {
        for (factory in DefaultOverlays.OVERLAYS) {
            overlays += factory.build(context) ?: continue
        }

        for (overlay in overlays) {
            overlay.init()
        }
    }

    fun postInit() {
        for (overlay in overlays) {
            overlay.postInit()
        }
    }

    override fun draw() {
        for (overlay in overlays) {
            overlay.update()
            if (!overlay.render) {
                continue
            }
            context.system.reset(blending = true, depthTest = false)
            overlay.draw()
        }
    }

    companion object {
        const val OVERLAY_Z = -0.1f
    }
}
