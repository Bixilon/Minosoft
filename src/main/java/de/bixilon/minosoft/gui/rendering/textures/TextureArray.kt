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

package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.gui.rendering.shader.Shader
import org.lwjgl.opengl.GL12.glTexImage3D
import org.lwjgl.opengl.GL12.glTexSubImage3D
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import org.lwjgl.opengl.GL30.glGenerateMipmap
import java.nio.ByteBuffer

class TextureArray(val textures: List<Texture>, val maxWidth: Int, val maxHeight: Int) {
    var textureId = 0
        private set

    fun load(): Int {
        textureId = glGenTextures()
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        // glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST) // ToDo: This breaks transparency again
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA, maxWidth, maxHeight, textures.size, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)

        for (texture in textures) {
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, texture.id, texture.width, texture.height, 1, GL_RGBA, GL_UNSIGNED_BYTE, texture.buffer)
        }
        glGenerateMipmap(GL_TEXTURE_2D_ARRAY)
        return textureId
    }


    fun use(textureMode: Int) {
        glActiveTexture(textureMode)
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
    }

    fun use(shader: Shader, arrayName: String) {
        glActiveTexture(GL_TEXTURE0 + textureId - 1)
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
        shader.use().setTexture(arrayName, this)
    }

    companion object {
        val DEBUG_TEXTURE = Texture("block/debug", 0)

        fun createTextureArray(assetsManager: AssetsManager? = null, textures: List<Texture>, maxWidth: Int = -1, maxHeight: Int = -1): TextureArray {
            var calculatedMaxWidth = 0
            var calculatedMaxHeight = 0
            for (texture in textures) {
                if (!texture.loaded) {
                    texture.load(assetsManager!!)
                }
                if (texture.width > calculatedMaxWidth) {
                    calculatedMaxWidth = texture.width
                }
                if (texture.height > calculatedMaxHeight) {
                    calculatedMaxHeight = texture.height
                }
            }
            if (maxWidth != -1) {
                calculatedMaxWidth = maxWidth
            }
            if (maxHeight != -1) {
                calculatedMaxHeight = maxWidth
            }
            // calculate width and height factor for every texture
            for (texture in textures) {
                texture.widthFactor = texture.width.toFloat() / calculatedMaxWidth
                texture.animations = (texture.height / texture.width)
                texture.heightFactor = texture.height.toFloat() / calculatedMaxHeight * (texture.width.toFloat() / texture.height)
            }

            return TextureArray(textures, calculatedMaxWidth, calculatedMaxHeight)
        }
    }
}
