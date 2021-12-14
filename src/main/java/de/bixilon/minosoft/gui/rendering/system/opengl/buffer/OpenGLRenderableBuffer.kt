package de.bixilon.minosoft.gui.rendering.system.opengl.buffer

import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferDrawTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferTypes
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER

abstract class OpenGLRenderableBuffer(override val type: RenderableBufferTypes) : RenderableBuffer {
    override var state: RenderableBufferStates = RenderableBufferStates.PREPARING
    abstract val drawTypes: RenderableBufferDrawTypes

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
        check(state == RenderableBufferStates.UPLOADED) { "Can not unload $state buffer!" }
        glDeleteBuffers(id)
        id = -1
        state = RenderableBufferStates.UNLOADED
    }


    protected companion object {
        val RenderableBufferTypes.gl: Int
            get() {
                return when (this) {
                    RenderableBufferTypes.UNIFORM_BUFFER -> GL_UNIFORM_BUFFER
                    RenderableBufferTypes.ARRAY_BUFFER -> GL_ARRAY_BUFFER
                    else -> throw IllegalArgumentException("OpenGL does not support buffer type: $this")
                }
            }

        val RenderableBufferDrawTypes.gl: Int
            get() {
                return when (this) {
                    RenderableBufferDrawTypes.DYNAMIC -> GL_DYNAMIC_DRAW
                    RenderableBufferDrawTypes.STATIC -> GL_STATIC_DRAW
                    else -> throw IllegalArgumentException("OpenGL does not support buffer draw type: $this")
                }
            }
    }
}
