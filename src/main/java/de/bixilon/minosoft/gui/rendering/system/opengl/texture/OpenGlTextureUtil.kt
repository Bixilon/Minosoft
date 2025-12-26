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

package de.bixilon.minosoft.gui.rendering.system.opengl.texture

import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGB8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGBA8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY

object OpenGlTextureUtil {

    fun createTextureArray(index: Int, mipmaps: Int): Int {
        assert(index >= 0)
        val textureId = gl { glGenTextures() }

        gl { glActiveTexture(GL_TEXTURE0 + index) }
        gl { glBindTexture(GL_TEXTURE_2D_ARRAY, textureId) }

        gl { glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT) }
        gl { glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT) }
        gl { glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, if (mipmaps == 0) GL_NEAREST else GL_NEAREST_MIPMAP_NEAREST) }
        gl { glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST) }
        gl { glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, mipmaps) }

        return textureId
    }

    val TextureBuffer.glFormat: Int
        get() = when (this) {
            is RGBA8Buffer -> GL_RGBA
            is RGB8Buffer -> GL_RGB
            // is RGBA2Buffer -> GL_RGBA
            else -> throw IllegalArgumentException("Can not get glFormat of $this")
        }
    val TextureBuffer.glType: Int
        get() = when (this) {
            is RGBA8Buffer -> GL_UNSIGNED_BYTE
            is RGB8Buffer -> GL_UNSIGNED_BYTE
            //  is RGBA2Buffer -> GL_UNSIGNED_BYTE
            else -> throw IllegalArgumentException("Can not get glFormat of $this")
        }
}
