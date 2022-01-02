package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.texture

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture

abstract class OpenGLTexture {
    var id: Int = -1
        protected set

    abstract fun init()

    fun bind(target: Int) {
        check(target in 0 until 12)
        glActiveTexture(GL_TEXTURE0 + target)
        glBindTexture(GL_TEXTURE_2D, id)
    }

    fun unload() {
        glDeleteTextures(id)
        id = -1
    }
}
