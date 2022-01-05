/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.FramebufferMesh
import de.bixilon.minosoft.gui.rendering.framebuffer.IntegratedFramebuffer
import de.bixilon.minosoft.gui.rendering.framebuffer.world.`fun`.FunManager
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.OverlayManager
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class WorldFramebuffer(
    override val renderWindow: RenderWindow,
) : IntegratedFramebuffer {
    private val overlay = OverlayManager(renderWindow)
    val `fun` = FunManager(renderWindow)
    private val defaultShader = renderWindow.renderSystem.createShader("minosoft:framebuffer/world".toResourceLocation())
    override val shader: Shader
        get() = `fun`.shader ?: defaultShader
    override val framebuffer: Framebuffer = renderWindow.renderSystem.createFramebuffer()
    override val mesh = FramebufferMesh(renderWindow)
    override var polygonMode: PolygonModes = PolygonModes.DEFAULT

    override fun init() {
        framebuffer.init()
        defaultShader.load()
        defaultShader.use()
        defaultShader.setInt("uColor", 0)
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
