/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.system.base.buffer.GpuBufferStates
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*

class OpenGlVao(
    val system: OpenGlRenderSystem,
    val struct: MeshStruct
) {
    var state = GpuBufferStates.PREPARING
        private set
    private var handle = -1

    fun init() {
        assert(state == GpuBufferStates.PREPARING)
        handle = gl { glGenVertexArrays() }
        unsafeBind()

        for (attribute in struct.attributes) {
            gl { glVertexAttribPointer(attribute.index, attribute.size, GL_FLOAT, false, struct.bytes, attribute.stride.toLong()) }
            gl { glEnableVertexAttribArray(attribute.index) }
        }
        unsafeUnbind()
        state = GpuBufferStates.INITIALIZED
    }

    private fun unsafeBind() {
        assert(handle >= 0)
        gl { glBindVertexArray(handle) }
        system.boundVao = handle
    }

    fun bind() {
        assert(state == GpuBufferStates.INITIALIZED)

        if (system.boundVao == handle) return
        unsafeBind()
    }

    private fun unsafeUnbind() {
        if (RenderConstants.DIRTY_BUFFER_UNBIND) {
            return
        }
        gl { glBindVertexArray(-1) }
        system.boundVao = -1
    }

    fun unbind() {
        assert(state == GpuBufferStates.INITIALIZED)
        unsafeUnbind()
    }

    fun unload() {
        assert(state == GpuBufferStates.INITIALIZED)
        gl { glDeleteVertexArrays(handle) }
        if (system.boundVao == handle) {
            system.boundVao = -1
        }
        handle = -1
        this.state = GpuBufferStates.UNLOADED
    }
}
