package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.minosoft.data.assets.AssetsManager
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.glTexImage3D
import org.lwjgl.opengl.GL12.glTexSubImage3D
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import org.lwjgl.opengl.GL30.glGenerateMipmap
import java.nio.ByteBuffer

class TextureArray(private val assetsManager: AssetsManager, private val textures: List<Texture>, private val size: Int = 16) {
    var textureId = 0

    fun load(): Int {
        textureId = glGenTextures()
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST)


        // load and generate the texture
        val textureMap: Map<Texture, ByteBuffer> = TextureLoader.loadTextureArray(assetsManager, textures)
        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA, size, size, textures.size, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)

        for ((key, value) in textureMap) {
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, key.id, size, size, 1, GL_RGBA, GL_UNSIGNED_BYTE, value)
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
    }
}
