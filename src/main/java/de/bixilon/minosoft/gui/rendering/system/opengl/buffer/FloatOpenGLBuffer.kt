package de.bixilon.minosoft.gui.rendering.system.opengl.buffer

import de.bixilon.minosoft.gui.rendering.system.base.buffer.FloatBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderBufferDrawTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderBufferTypes
import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL15.glBufferSubData

open class FloatOpenGLBuffer(protected var _data: FloatArray?) : OpenGLRenderBuffer(RenderBufferTypes.ARRAY_BUFFER), FloatBuffer {
    override var data: FloatArray
        get() = _data!!
        set(value) {
            _data = value
        }
    override val drawTypes: RenderBufferDrawTypes = RenderBufferDrawTypes.STATIC

    override fun initialUpload() {
        bind()
        glBufferData(type.gl, data, drawTypes.gl)
        unbind()
    }

    override fun upload() {
        bind()
        glBufferSubData(type.gl, 0, data)
        unbind()
    }
}