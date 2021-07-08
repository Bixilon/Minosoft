package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.vertex

import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.VertexBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.FloatOpenGLBuffer
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct.Companion.BYTES
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.Util
import org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray
import org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.primaryConstructor

class FloatOpenGLVertexBuffer(override val structure: KClass<*>, data: FloatArray, override val primitiveType: PrimitiveTypes = PrimitiveTypes.TRIANGLE) : FloatOpenGLBuffer(data), VertexBuffer {
    var vertices = -1
        private set
    private var vao = -1

    override fun init() {
        super.init()

        Util.forceClassInit(structure.java)

        val bytesPerVertex = structure.companionObjectInstance!!.unsafeCast<MeshStruct>().BYTES_PER_VERTEX

        val floatsPerVertex = bytesPerVertex / Float.SIZE_BYTES

        vertices = data.size / floatsPerVertex
        vao = glGenVertexArrays()
        glBindVertexArray(vao)
        super.init()
        super.initialUpload()

        _data = null


        var stride = 0L

        for ((index, parameter) in structure.primaryConstructor!!.parameters.withIndex()) {
            val bytes = parameter.BYTES
            glVertexAttribPointer(index, bytes / Float.SIZE_BYTES, GL_FLOAT, false, bytesPerVertex, stride)
            glEnableVertexAttribArray(index)
            stride += bytes
        }
        unbind()
    }

    override fun draw() {
        glBindVertexArray(vao)
        glDrawArrays(primitiveType.gl, 0, vertices)
    }


    private companion object {
        val PrimitiveTypes.gl: Int
            get() {
                return when (this) {
                    PrimitiveTypes.POINT -> GL_POINTS
                    PrimitiveTypes.LINE -> GL_LINE
                    PrimitiveTypes.TRIANGLE -> GL_TRIANGLES
                    PrimitiveTypes.QUADS -> GL_QUADS
                }
            }
    }

}