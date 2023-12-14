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

import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*

class OpenGLVAO(
    val system: OpenGLRenderSystem,
    val struct: MeshStruct
) {
    private var handle = -1

    fun init() {
        if (handle >= 0) throw IllegalArgumentException("VAO already loaded!")
        handle = glGenVertexArrays()
        bind()

        for (attribute in struct.attributes) {
            glVertexAttribPointer(attribute.index, attribute.size, GL_FLOAT, false, struct.BYTES_PER_VERTEX, attribute.stride)
            glEnableVertexAttribArray(attribute.index)
        }
        unbind()
    }

    fun bind() {
        if (handle < 0) throw IllegalArgumentException("VAO not initialized!")
        if (system.boundVao == handle) {
            return
        }
        glBindVertexArray(handle)
        system.boundVao = handle
    }

    fun unbind() {
        if (RenderConstants.DIRTY_BUFFER_UNBIND) {
            return
        }
        if (handle < 0) throw IllegalArgumentException("VAO not initialized!")
        glBindVertexArray(-1)
        system.boundVao = -1
    }

    fun unload() {
        if (handle < 0) throw IllegalArgumentException("VAO not initialized!")
        glDeleteVertexArrays(handle)
        if (system.boundVao == handle) {
            system.boundVao = -1
        }
        handle = -1
    }
}
