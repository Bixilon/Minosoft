package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame

import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.FramebufferState
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.texture.FramebufferTexture
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.Renderbuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.RenderbufferModes
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.texture.OpenGLFramebufferTexture
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.render.OpenGLRenderbuffer
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL30.*

class OpenGLFramebuffer : Framebuffer {
    override var state: FramebufferState = FramebufferState.PREPARING
        private set

    private var id = -1

    private lateinit var texture: OpenGLFramebufferTexture

    override fun init() {
        check(state == FramebufferState.PREPARING) { "Framebuffer is already (un)loaded!" }
        id = glGenFramebuffers()
        unsafeBind()

        texture = OpenGLFramebufferTexture(Vec2i(900, 500)) // ToDo
        texture.init()
        attach(texture)

        val renderbuffer = OpenGLRenderbuffer(RenderbufferModes.DEPTH_24_STENCIL_8, Vec2i(900, 500)) // ToDo
        renderbuffer.init()
        attach(renderbuffer)

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

    fun unbind() {
        check(state == FramebufferState.COMPLETE) { "Framebuffer is incomplete!" }
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    override fun attach(renderbuffer: Renderbuffer) {
        check(renderbuffer is OpenGLRenderbuffer) { "Can not attach non OpenGL renderbuffer!" }
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, renderbuffer.id)
    }

    override fun attach(texture: FramebufferTexture) {
        check(texture is OpenGLFramebufferTexture) { "Can not attach non OpenGL texture!" }
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture.id, 0)
    }

    override fun delete() {
        check(state == FramebufferState.COMPLETE) { "Framebuffer is incomplete!" }
        glDeleteFramebuffers(id)
        state = FramebufferState.DELETED
    }

    override fun bindTexture() {
        glBindTexture(GL_TEXTURE_2D, texture.id)
    }
}
