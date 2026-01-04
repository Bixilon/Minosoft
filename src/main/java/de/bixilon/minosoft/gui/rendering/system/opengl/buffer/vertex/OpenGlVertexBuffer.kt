/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
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
import de.bixilon.minosoft.gui.rendering.system.base.buffer.GpuBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.VertexBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.FloatOpenGlBuffer
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.opengl.GL30.*

class OpenGlVertexBuffer(
    system: OpenGlRenderSystem,
    override val primitive: PrimitiveTypes,
    override val struct: MeshStruct,
    val data: FloatOpenGlBuffer,
    val index: OpenGlIndexBuffer?,
) : VertexBuffer {
    override var state = GpuBufferStates.PREPARING
        private set
    private val vao = OpenGlVao(system, struct)
    override var vertices = -1
        private set


    private fun prepareInit() {
        assert(state == GpuBufferStates.PREPARING)
        this.state = GpuBufferStates.INITIALIZING

        vertices = when {
            EMPTY_BUFFERS -> 0
            index != null -> index.data.limit()
            else -> data.data.limit() / struct.floats
        }

        if (vertices == 0) {
            Log.log(LogMessageType.RENDERING, LogLevels.WARN) { "Empty vertex buffer: $this" }
        }
    }

    override fun init() {
        prepareInit()

        data.init()
        data.bind()

        vao.init()

        index?.init()

        unbind()
        state = GpuBufferStates.INITIALIZED
    }

    private fun unbind() {
        data.unbind()
        vao.unbind()
        index?.unbind()
    }


    override fun draw() {
        check(state == GpuBufferStates.INITIALIZED) { "Vertex buffer is not uploaded: $state" }

        vao.bind()

        if (index == null) {
            gl { glDrawArrays(primitive.gl, 0, vertices) }
        } else {
            index.bind()
            gl { glDrawElements(primitive.gl, vertices, GL_UNSIGNED_INT, 0) }
            index.unbind()
        }

        vao.unbind()
    }

    override fun drop() {
        assert(state == GpuBufferStates.PREPARING)
        data.drop()
        index?.drop()

        state = GpuBufferStates.UNLOADED
    }

    override fun unload() {
        check(state == GpuBufferStates.INITIALIZED) { "Vertex buffer is not uploaded: $state" }
        data.unload()
        vao.unload()
        index?.unload()

        state = GpuBufferStates.UNLOADED
    }

    override fun toString() = "OpenGlVertexBuffer(vertices=$vertices, state=$state)"

    private companion object {

        val PrimitiveTypes.gl: Int
            get() = when (this) {
                PrimitiveTypes.POINT -> GL_POINTS
                PrimitiveTypes.LINE -> GL_LINES
                PrimitiveTypes.TRIANGLE -> GL_TRIANGLES
                PrimitiveTypes.QUAD -> GL_QUADS
            }
    }
}
