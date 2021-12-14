package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.render

import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.Renderbuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.RenderbufferModes
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL30.*

class OpenGLRenderbuffer(
    override val mode: RenderbufferModes,
    override val size: Vec2i,
) : Renderbuffer {
    var id = -1
        private set

    override fun init() {
        id = glGenRenderbuffers()
        bind()
        glRenderbufferStorage(GL_RENDERBUFFER, mode.gl, size.x, size.y)
    }

    fun bind() {
        glBindRenderbuffer(GL_RENDERBUFFER, id)
    }

    override fun unload() {
        glDeleteRenderbuffers(id)
        id = -1
    }

    companion object {
        private val RenderbufferModes.gl: Int
            get() {
                return when (this) {
                    RenderbufferModes.DEPTH_COMPONENT16 -> GL_DEPTH_COMPONENT16
                    RenderbufferModes.DEPTH_COMPONENT24 -> GL_DEPTH_COMPONENT24
                    RenderbufferModes.DEPTH_COMPONENT32F -> GL_DEPTH_COMPONENT32
                    RenderbufferModes.DEPTH_24_STENCIL_8 -> GL_DEPTH24_STENCIL8
                    RenderbufferModes.DEPTH_32F_STENCIL_8 -> GL_DEPTH32F_STENCIL8
                    RenderbufferModes.STENCIL_INDEX8 -> GL_STENCIL_INDEX8
                    else -> throw IllegalArgumentException("OpenGL does not support renderbuffer mode: $this")
                }
            }
    }
}
