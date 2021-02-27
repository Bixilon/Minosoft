/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.textures.Texture
import glm_.BYTES
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.*

class ChunkMesh {
    private val data: MutableList<Float> = mutableListOf()
    private var vao: Int = 0
    private var vbo: Int = 0
    private var trianglesCount: Int = 0

    fun addVertex(position: Vec3, textureCoordinates: Vec2, texture: Texture, tintColor: RGBColor?, lightLevel: Byte = 14) {
        data.add(position.x)
        data.add(position.y)
        data.add(position.z)
        data.add(textureCoordinates.x * texture.widthFactor)
        data.add(textureCoordinates.y * texture.heightFactor)
        data.add(Float.fromBits(texture.id)) // ToDo: Compact this

        // ToDo: Send this only once per texture
        data.add(texture.animationFrameTime.toFloat())
        data.add(texture.animations.toFloat())
        data.add(texture.heightFactor)

        if (tintColor == null) {
            data.add(0f)
        } else {
            data.add(Float.fromBits(tintColor.color))
        }

        data.add(lightLevel / MAX_LIGHT_LEVEL)
    }

    fun load() {
        trianglesCount = data.size / FLOATS_PER_VERTEX
        vao = glGenVertexArrays()
        vbo = glGenBuffers()

        // bind the Vertex Array Object first, then bind and set vertex buffer(s), and then configure vertex attributes(s).
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, data.toFloatArray(), GL_STATIC_DRAW)
        data.clear() // clear data ((do not store in memory)
        var index = 0
        glVertexAttribPointer(index, 3, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, 0L)
        glEnableVertexAttribArray(index++)

        glVertexAttribPointer(index, 2, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, (3 * Float.BYTES).toLong())
        glEnableVertexAttribArray(index++)

        glVertexAttribPointer(index, 1, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, (5 * Float.BYTES).toLong())
        glEnableVertexAttribArray(index++)

        glVertexAttribPointer(index, 3, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, (6 * Float.BYTES).toLong())
        glEnableVertexAttribArray(index++)

        glVertexAttribPointer(index, 1, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, (9 * Float.BYTES).toLong())
        glEnableVertexAttribArray(index++)

        glVertexAttribPointer(index, 1, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.BYTES, (10 * Float.BYTES).toLong())
        glEnableVertexAttribArray(index++)

        // don't remove the ++ above!
        index.let { }

        // note that this is allowed, the call to glVertexAttribPointer registered VBO as the vertex attribute's bound vertex buffer object so afterwards we can safely unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    fun draw() {
        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, trianglesCount)
    }

    fun unload() {
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
    }

    companion object {
        private const val FLOATS_PER_VERTEX = 11
        private const val MAX_LIGHT_LEVEL = 15f
    }
}
