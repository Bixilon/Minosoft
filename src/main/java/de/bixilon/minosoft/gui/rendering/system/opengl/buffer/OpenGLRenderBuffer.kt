package de.bixilon.minosoft.gui.rendering.system.opengl.buffer

import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderBufferDrawTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderBufferTypes
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER

abstract class OpenGLRenderBuffer(override val type: RenderBufferTypes) : RenderBuffer {
    override var state: RenderBufferStates = RenderBufferStates.PREPARING
    abstract val drawTypes: RenderBufferDrawTypes

    protected var id: Int = -1
        private set

    override fun init() {
        id = glGenBuffers()
    }

    override fun bind() {
        glBindBuffer(type.gl, id)
    }

    override fun unbind() {
        glBindBuffer(type.gl, 0)
    }

    override fun unload() {
        check(state == RenderBufferStates.UPLOADED) { "Can not unload $state buffer!" }
        glDeleteBuffers(id)
        id = -1
        state = RenderBufferStates.UNLOADED
    }


    protected companion object {
        val RenderBufferTypes.gl: Int
            get() {
                return when (this) {
                    RenderBufferTypes.UNIFORM_BUFFER -> GL_UNIFORM_BUFFER
                    RenderBufferTypes.ARRAY_BUFFER -> GL_ARRAY_BUFFER
                    else -> throw IllegalArgumentException("OpenGL does not support buffer type: $this")
                }
            }

        val RenderBufferDrawTypes.gl: Int
            get() {
                return when (this) {
                    RenderBufferDrawTypes.DYNAMIC -> GL_DYNAMIC_DRAW
                    RenderBufferDrawTypes.STATIC -> GL_STATIC_DRAW
                    else -> throw IllegalArgumentException("OpenGL does not support buffer draw type: $this")
                }
            }
    }
}
