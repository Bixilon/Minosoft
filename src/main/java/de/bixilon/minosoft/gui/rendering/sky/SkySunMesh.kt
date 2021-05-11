/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.sky

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.util.Mesh
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer

class SkySunMesh : Mesh(initialCacheSize = 2 * 3 * FLOATS_PER_VERTEX) {

    fun addVertex(position: Vec3, texture: Texture, textureCoordinates: Vec2, tintColor: RGBColor) {
        val textureLayer = if (RenderConstants.FORCE_DEBUG_TEXTURE) {
            RenderConstants.DEBUG_TEXTURE_ID
        } else {
            (texture.arrayId shl 24) or texture.arrayLayer
        }

        data.addAll(floatArrayOf(
            position.x,
            position.y,
            position.z,
            textureCoordinates.x,
            textureCoordinates.y,
            Float.fromBits(textureLayer),
            Float.fromBits(tintColor.rgba),
        ))
    }

    override fun load() {
        super.initializeBuffers(FLOATS_PER_VERTEX)
        var index = 0
        glVertexAttribPointer(index, 3, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.SIZE_BYTES, 0L)
        glEnableVertexAttribArray(index++)
        glVertexAttribPointer(index, 2, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.SIZE_BYTES, (3 * Float.SIZE_BYTES).toLong())
        glEnableVertexAttribArray(index++)
        glVertexAttribPointer(index, 1, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.SIZE_BYTES, (5 * Float.SIZE_BYTES).toLong())
        glEnableVertexAttribArray(index++)
        glVertexAttribPointer(index, 1, GL_FLOAT, false, FLOATS_PER_VERTEX * Float.SIZE_BYTES, (6 * Float.SIZE_BYTES).toLong())
        glEnableVertexAttribArray(index++)
        super.unbind()
    }


    companion object {
        private const val FLOATS_PER_VERTEX = 7
    }
}
