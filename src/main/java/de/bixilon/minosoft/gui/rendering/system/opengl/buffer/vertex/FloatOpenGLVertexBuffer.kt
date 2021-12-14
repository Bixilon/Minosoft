package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.vertex

import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.FloatVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.FloatOpenGLBuffer
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*
import java.nio.FloatBuffer

class FloatOpenGLVertexBuffer(override val structure: MeshStruct, data: FloatBuffer, override val primitiveType: PrimitiveTypes) : FloatOpenGLBuffer(data), FloatVertexBuffer {
    override var vertices = -1
        private set
    private var vao = -1

    override fun init() {
        val floatsPerVertex = structure.BYTES_PER_VERTEX / Float.SIZE_BYTES

        vertices = buffer.position() / floatsPerVertex
        vao = glGenVertexArrays()
        super.init()
        glBindVertexArray(vao)

        bind()
        val previousLimit = buffer.limit()
        val previousPosition = buffer.position()
        buffer.limit(buffer.position())
        buffer.flip()
        glBufferData(type.gl, buffer, drawTypes.gl)
        buffer.limit(previousLimit)
        buffer.position(previousPosition)
        state = RenderableBufferStates.UPLOADED

        _data = null


        for (attribute in structure.attributes) {
            glVertexAttribPointer(attribute.index, attribute.size, GL_FLOAT, false, structure.BYTES_PER_VERTEX, attribute.stride)
            glEnableVertexAttribArray(attribute.index)
        }

        unbind()
    }

    override fun draw() {
        check(state == RenderableBufferStates.UPLOADED) { "Can not draw $state vertex buffer!" }
        glBindVertexArray(vao)
        glDrawArrays(primitiveType.gl, 0, vertices)
    }

    override fun unload() {
        if (state == RenderableBufferStates.UPLOADED) {
            glDeleteVertexArrays(vao)
            vao = -1
        }
        super.unload()
    }


    private companion object {
        val PrimitiveTypes.gl: Int
            get() {
                return when (this) {
                    PrimitiveTypes.POINT -> GL_POINTS
                    PrimitiveTypes.LINE -> GL_LINES
                    PrimitiveTypes.TRIANGLE -> GL_TRIANGLES
                    PrimitiveTypes.QUAD -> GL_QUADS
                }
            }
    }

}
