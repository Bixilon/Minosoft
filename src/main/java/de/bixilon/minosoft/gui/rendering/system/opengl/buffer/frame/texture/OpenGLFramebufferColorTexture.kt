package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.texture

import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.texture.FramebufferTexture
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL30.*
import java.nio.ByteBuffer

class OpenGLFramebufferColorTexture(
    override val size: Vec2i,
) : OpenGLTexture(), FramebufferTexture {

    override fun init() {
        id = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, id)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size.x, size.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    }
}
