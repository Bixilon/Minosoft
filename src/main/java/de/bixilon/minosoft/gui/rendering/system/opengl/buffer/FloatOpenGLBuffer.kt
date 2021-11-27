package de.bixilon.minosoft.gui.rendering.system.opengl.buffer

import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderBufferDrawTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderBufferTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderFloatBuffer
import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL15.glBufferSubData
import java.nio.FloatBuffer

open class FloatOpenGLBuffer(protected var _data: FloatBuffer?) : OpenGLRenderBuffer(RenderBufferTypes.ARRAY_BUFFER), RenderFloatBuffer {
    override var buffer: FloatBuffer
        get() = _data!!
        set(value) {
            _data = value
        }
    override val drawTypes: RenderBufferDrawTypes = RenderBufferDrawTypes.STATIC

    override fun initialUpload() {
        bind()
        buffer.flip()
        glBufferData(type.gl, buffer, drawTypes.gl)
        unbind()
        state = RenderBufferStates.UPLOADED
    }

    override fun upload() {
        bind()
        glBufferSubData(type.gl, 0, buffer)
        unbind()
    }
}
