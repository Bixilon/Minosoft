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

package de.bixilon.minosoft.gui.rendering.framebuffer.world

import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.framebuffer.FramebufferMesh
import de.bixilon.minosoft.gui.rendering.framebuffer.FramebufferShader
import de.bixilon.minosoft.gui.rendering.framebuffer.IntegratedFramebuffer
import de.bixilon.minosoft.gui.rendering.framebuffer.world.`fun`.FunEffectManager
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayManager
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class WorldFramebuffer(
    override val context: RenderContext,
) : IntegratedFramebuffer {
    private val overlay = OverlayManager(context)
    val `fun` = FunEffectManager(context)
    private val defaultShader = context.system.createShader("minosoft:framebuffer/world".toResourceLocation()) { FramebufferShader(it) }
    override val shader: FramebufferShader
        get() = `fun`.shader ?: defaultShader
    override val framebuffer: Framebuffer = context.system.createFramebuffer(color = true, depth = true)
    override val mesh = FramebufferMesh(context)
    override var polygonMode: PolygonModes = PolygonModes.DEFAULT

    override fun init() {
        framebuffer.init()
        defaultShader.load()
        defaultShader.use()
        // shader.setInt("uDepth", 1)
        mesh.load()

        overlay.init()
    }

    override fun postInit() {
        super.postInit()
        overlay.postInit()
    }

    override fun draw() {
        shader.use()
        `fun`.preDraw()
        super.draw()
        overlay.draw()
    }
}
