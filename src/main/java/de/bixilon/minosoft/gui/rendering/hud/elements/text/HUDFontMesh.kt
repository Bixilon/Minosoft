/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.elements.text

import glm_.BYTES
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*

class HUDFontMesh(data: FloatArray) {
    var vAO: Int = glGenVertexArrays()
    var vBO: Int = glGenBuffers()
    var trianglesCount: Int = data.size / FLOAT_PER_VERTEX

    fun draw() {
        glBindVertexArray(vAO)
        glDrawArrays(GL_TRIANGLES, 0, trianglesCount)
    }

    fun unload() {
        glDeleteVertexArrays(vAO)
        glDeleteBuffers(vBO)
    }

    init {
        // bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vAO)
        glBindBuffer(GL_ARRAY_BUFFER, vBO)
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, FLOAT_PER_VERTEX * Float.BYTES, 0L)
        glEnableVertexAttribArray(0)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, FLOAT_PER_VERTEX * Float.BYTES, (3 * Float.BYTES).toLong())
        glEnableVertexAttribArray(1)
        glVertexAttribPointer(2, 1, GL_FLOAT, false, FLOAT_PER_VERTEX * Float.BYTES, (5 * Float.BYTES).toLong())
        glEnableVertexAttribArray(2)
        glVertexAttribPointer(3, 1, GL_FLOAT, false, FLOAT_PER_VERTEX * Float.BYTES, (6 * Float.BYTES).toLong())
        glEnableVertexAttribArray(3)

        // note that this is allowed, the call to glVertexAttribPointer registered VBO as the vertex attribute's bound vertex buffer object so afterwards we can safely unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    companion object {
        private const val FLOAT_PER_VERTEX = 7
    }
}
