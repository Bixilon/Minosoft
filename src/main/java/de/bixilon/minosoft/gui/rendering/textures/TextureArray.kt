package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.minosoft.data.assets.AssetsManager
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.glTexImage3D
import org.lwjgl.opengl.GL12.glTexSubImage3D
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import org.lwjgl.opengl.GL30.glGenerateMipmap
import java.nio.ByteBuffer

class TextureArray(private val textures: List<Texture>, val maxWidth: Int, val maxHeight: Int) {
    var textureId = 0

    fun load(): Int {
        textureId = glGenTextures()
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
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

    companion object {
        val DEBUG_TEXTURE = Texture("block/debug", 0)
        val TRANSPARENT_TEXTURES = listOf("block/glass")

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
            return TextureArray(textures, calculatedMaxWidth, calculatedMaxHeight)
        }
    }
}
