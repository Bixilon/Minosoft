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

import de.bixilon.minosoft.data.assets.MinecraftAssetsManager
import de.bixilon.minosoft.gui.rendering.shader.Shader
import org.lwjgl.opengl.GL12.glTexImage3D
import org.lwjgl.opengl.GL12.glTexSubImage3D
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import java.nio.ByteBuffer

class TextureArray(val textures: MutableList<Texture>) {
    var textureId = 0
        private set
    var maxWidth: Int = 0
        private set
    var maxHeight: Int = 0
        private set


    fun preLoad(assetsManager: MinecraftAssetsManager?) {
        for (texture in textures) {
            if (!texture.loaded) {
                texture.load(assetsManager!!)
            }
            if (texture.width > maxWidth) {
                maxWidth = texture.width
            }
            if (texture.height > maxHeight) {
                maxHeight = texture.height
            }
        }

        // calculate width and height factor for every texture
        for ((index, texture) in textures.withIndex()) {
            texture.widthFactor = texture.width.toFloat() / maxWidth
            texture.animations = (texture.height / texture.width)
            texture.heightFactor = texture.height.toFloat() / maxHeight * (texture.width.toFloat() / texture.height)
            texture.layer = index
        }
    }

    fun load(): Int {
        textureId = glGenTextures()
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        // glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR) // ToDo: This breaks transparency again
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA, maxWidth, maxHeight, textures.size, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)

        for (texture in textures) {
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, texture.layer, texture.width, texture.height, 1, GL_RGBA, GL_UNSIGNED_BYTE, texture.buffer)
            texture.buffer.clear()
        }
      //  glGenerateMipmap(GL_TEXTURE_2D_ARRAY)
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
        val DEBUG_TEXTURE = Texture.getResourceTextureIdentifier(textureName = "block/debug")
    }
}
