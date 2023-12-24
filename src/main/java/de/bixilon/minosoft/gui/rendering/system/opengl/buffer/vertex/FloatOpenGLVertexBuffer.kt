/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.vertex

import de.bixilon.minosoft.config.DebugOptions.EMPTY_BUFFERS
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.FloatVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.FloatOpenGLBuffer
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import org.lwjgl.opengl.GL30.*
import java.nio.FloatBuffer

class FloatOpenGLVertexBuffer(
    renderSystem: OpenGLRenderSystem,
    override val struct: MeshStruct,
    data: FloatBuffer,
    override val primitiveType: PrimitiveTypes,
) : FloatOpenGLBuffer(renderSystem, data), FloatVertexBuffer {
    private val vao = OpenGLVAO(renderSystem, struct)
    override var vertices = -1
        private set

    override fun init() {
        val floatsPerVertex = struct.BYTES_PER_VERTEX / Float.SIZE_BYTES

        vertices = buffer.position() / floatsPerVertex
        super.init()
        bind()
        super.initialUpload()
        _data = null

        vao.init()

        unbind()
    }

    override fun unbind() {
        if (RenderConstants.DIRTY_BUFFER_UNBIND) {
            return
        }
        super.unbind()
        vao.unbind()
    }

    fun bindVao() {
        super.bind()
        vao.bind()
    }

    override fun draw() {
        check(state == RenderableBufferStates.UPLOADED) { "Vertex buffer is not uploaded: $state" }
        bindVao()
        glDrawArrays(primitiveType.gl, 0, if (EMPTY_BUFFERS) 0 else vertices)
    }

    override fun unload() {
        vao.unload()
        super.unload()
    }

    override fun toString() = "FloatOpenGLVertexBuffer(vertices=$vertices, state=$state)"

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
