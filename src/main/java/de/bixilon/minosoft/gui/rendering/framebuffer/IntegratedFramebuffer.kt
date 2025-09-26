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

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.gui.rendering.system.base.PolygonModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh

interface IntegratedFramebuffer : Drawable {
    val context: RenderContext
    val shader: FramebufferShader
    var framebuffer: Framebuffer
    val mesh: Mesh
    val polygonMode: PolygonModes

    var size: Vec2i
    var scale: Float


    fun init() {
        framebuffer = create() // unsafeNull
        framebuffer.init()
        shader.load()
        // shader.setInt("uDepth", 1)
        mesh.load()
    }

    fun postInit() {}

    fun clear() {
        context.system.framebuffer = framebuffer
        context.system.clear(IntegratedBufferTypes.COLOR_BUFFER, IntegratedBufferTypes.DEPTH_BUFFER)
    }

    fun create(): Framebuffer

    fun update() {
        if (this.size == framebuffer.size && this.scale == framebuffer.scale) return

        framebuffer.delete()
        framebuffer = create()
        framebuffer.init()
    }

    fun bind() {
        context.system.framebuffer = framebuffer
        context.system.polygonMode = polygonMode
    }

    override fun draw() {
        context.system.framebuffer = null
        context.system.reset(
            blending = true,
            sourceRGB = BlendingFunctions.SOURCE_ALPHA,
            destinationRGB = BlendingFunctions.ONE_MINUS_SOURCE_ALPHA,
        )
        framebuffer.bindTexture()
        shader.use()
        mesh.draw()
    }
}
