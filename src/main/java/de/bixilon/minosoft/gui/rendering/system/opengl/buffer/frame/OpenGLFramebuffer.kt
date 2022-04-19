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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.FramebufferState
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.texture.FramebufferTexture
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.Renderbuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.RenderbufferModes
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.texture.OpenGLFramebufferColorTexture
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.texture.OpenGLFramebufferDepthTexture
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.render.OpenGLRenderbuffer
import org.lwjgl.opengl.GL30.*

class OpenGLFramebuffer(var size: Vec2i) : Framebuffer {
    override var state: FramebufferState = FramebufferState.PREPARING
        private set

    private var id = -1

    private lateinit var colorTexture: OpenGLFramebufferColorTexture
    private lateinit var depthTexture: OpenGLFramebufferDepthTexture
    private lateinit var renderbuffer: OpenGLRenderbuffer

    override fun init() {
        check(state != FramebufferState.COMPLETE) { "Framebuffer is complete!" }
        id = glGenFramebuffers()
        unsafeBind()

        glViewport(0, 0, size.x, size.y)

        colorTexture = OpenGLFramebufferColorTexture(size)
        colorTexture.init()
        attach(colorTexture)

        renderbuffer = OpenGLRenderbuffer(RenderbufferModes.DEPTH_COMPONENT24, size)
        renderbuffer.init()
        attach(renderbuffer)

        //depthTexture = OpenGLFramebufferDepthTexture(size)
        //depthTexture.init()
        //attach(depthTexture)

        check(glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE) { "Framebuffer is incomplete!" }
        state = FramebufferState.COMPLETE
    }

    fun bind() {
        check(state == FramebufferState.COMPLETE) { "Framebuffer is incomplete!" }
        unsafeBind()
    }

    private fun unsafeBind() {
        glBindFramebuffer(GL_FRAMEBUFFER, id)
    }

    override fun attach(renderbuffer: Renderbuffer) {
        check(renderbuffer is OpenGLRenderbuffer) { "Can not attach non OpenGL renderbuffer!" }
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderbuffer.id)
    }

    override fun attach(texture: FramebufferTexture) {
        when (texture) {
            is OpenGLFramebufferDepthTexture -> glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texture.id, 0)
            is OpenGLFramebufferColorTexture -> glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.id, 0)
            else -> throw IllegalArgumentException("Can not attach non OpenGL texture!")
        }
    }

    override fun delete() {
        check(state == FramebufferState.COMPLETE) { "Framebuffer is incomplete!" }
        glDeleteFramebuffers(id)
        id = -1
        state = FramebufferState.DELETED
    }

    override fun bindTexture() {
        check(state == FramebufferState.COMPLETE) { "Framebuffer is incomplete!" }
        colorTexture.bind(0)
        if (this::depthTexture.isInitialized) {
            depthTexture.bind(1)
        }
    }

    override fun resize(size: Vec2i) {
        if (size == this.size) {
            return
        }
        if (this::colorTexture.isInitialized) {
            colorTexture.unload()
        }
        if (this::renderbuffer.isInitialized) {
            renderbuffer.unload()
        }
        this.size = size
        delete()
        init()
    }
}
