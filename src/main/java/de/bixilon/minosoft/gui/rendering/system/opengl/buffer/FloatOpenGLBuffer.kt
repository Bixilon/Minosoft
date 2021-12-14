package de.bixilon.minosoft.gui.rendering.system.opengl.buffer

import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderFloatBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferDrawTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferTypes
import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL15.glBufferSubData
import java.nio.FloatBuffer

open class FloatOpenGLBuffer(protected var _data: FloatBuffer?) : OpenGLRenderableBuffer(RenderableBufferTypes.ARRAY_BUFFER), RenderFloatBuffer {
    override var buffer: FloatBuffer
        get() = _data!!
        set(value) {
            _data = value
        }
    override val drawTypes: RenderableBufferDrawTypes = RenderableBufferDrawTypes.STATIC

    override fun initialUpload() {
        bind()
        buffer.flip()
        glBufferData(type.gl, buffer, drawTypes.gl)
        unbind()
        state = RenderableBufferStates.UPLOADED
    }

    override fun upload() {
        bind()
        glBufferSubData(type.gl, 0, buffer)
        unbind()
    }
}
