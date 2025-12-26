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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.FramebufferState
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.depth.DepthModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.stencil.StencilModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.texture.TextureModes
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.attachment.OpenGlBufferAttachment
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.attachment.depth.OpenGlDepthAttachment
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.attachment.stencil.OpenGlStencilAttachment
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.attachment.texture.OpenGlTextureAttachment
import de.bixilon.minosoft.gui.rendering.system.opengl.error.MemoryLeakException
import org.lwjgl.opengl.GL30.*

class OpenGlFramebuffer(
    val system: OpenGlRenderSystem,
    override val size: Vec2i,
    override val scale: Float,
    texture: TextureModes? = null,
    depth: DepthModes? = null,
    stencil: StencilModes? = null,
) : Framebuffer {
    private var scaled = size
    override var state: FramebufferState = FramebufferState.PREPARING
        private set

    private var id = -1

    override val texture = texture?.let { OpenGlTextureAttachment(system, size, it) }
    override val depth = depth?.let { OpenGlDepthAttachment(system, size, it) }
    override val stencil = stencil?.let { OpenGlStencilAttachment(system, size, it) }


    init {
        if (texture == null && depth == null && stencil == null) throw IllegalArgumentException("This framebuffer does nothing!")
        if (scale <= 0.0f) throw IllegalArgumentException("Invalid scale: $scale")
        if (size.x <= 0 || size.y <= 0) throw IllegalArgumentException("Invalid framebuffer size: $size")
    }

    override fun init() {
        check(state == FramebufferState.PREPARING) { "Framebuffer was already initialized!" }
        system.log { "Init framebuffer $this" }
        id = gl { glGenFramebuffers() }
        unsafeBind()

        this.scaled = if (scale == 1.0f) size else Vec2i((size.x * scale).toInt(), (size.y * scale).toInt())

        if (texture != null) {
            texture.init()
            attach(texture)
        }

        if (depth != null) {
            depth.init()
            attach(depth)
        }

        if (stencil != null) {
            stencil.init()
            attach(stencil)
        }

        val state = gl { glCheckFramebufferStatus(GL_FRAMEBUFFER) }
        check(state == GL_FRAMEBUFFER_COMPLETE) { "Framebuffer is incomplete: $state" }
        this.state = FramebufferState.COMPLETE
    }

    fun bind() {
        check(state == FramebufferState.COMPLETE) { "Framebuffer is incomplete: $state" }
        unsafeBind()
        system.viewport = scaled
    }

    private fun unsafeBind() {
        system.log { "Binding framebuffer $this" }
        gl { glBindFramebuffer(GL_FRAMEBUFFER, id) }
    }

    private fun attach(renderbuffer: OpenGlBufferAttachment) {
        gl { glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderbuffer.id) }
    }

    private fun attach(texture: OpenGlTextureAttachment) {
        gl { glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.id, 0) }
    }

    override fun delete() {
        check(state == FramebufferState.COMPLETE) { "Framebuffer is incomplete: $state" }

        texture?.unload()
        depth?.unload()
        stencil?.unload()

        gl { glDeleteFramebuffers(id) }
        id = -1
        state = FramebufferState.DELETED
    }

    override fun bindTexture() {
        check(state == FramebufferState.COMPLETE) { "Framebuffer is incomplete: $state" }
        texture?.bind()
    }

    protected fun finalize() {
        if (state == FramebufferState.COMPLETE && system.active) {
            MemoryLeakException("Framebuffer has not been unloaded!").printStackTrace()
        }
    }

    override fun toString() = "OpenGLFramebuffer(size=$size, texture=$texture, depth=$depth, state=$state)"
}
