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

package de.bixilon.minosoft.gui.rendering.framebuffer.gui

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.FramebufferMesh
import de.bixilon.minosoft.gui.rendering.framebuffer.FramebufferShader
import de.bixilon.minosoft.gui.rendering.framebuffer.IntegratedFramebuffer
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class GUIFramebuffer(
    override val context: RenderContext,
) : IntegratedFramebuffer {
    override val shader = context.system.shader.create("minosoft:framebuffer/gui".toResourceLocation()) { FramebufferShader(it) }
    override var framebuffer: Framebuffer = unsafeNull()
    override val mesh = FramebufferMesh(context)
    override var polygonMode: PolygonModes = PolygonModes.DEFAULT

    override var scale = 1.0f
    override var size = Vec2i(1, 1)

    override fun init() {
        super.init()
        context.session.profiles.rendering.quality.resolution::guiScale.observe(this, instant = true) { this.scale = it }
    }

    override fun create() = context.system.createFramebuffer(this.size, this.scale, color = true, depth = false)

    override fun draw() {
        super.draw()
    }
}
