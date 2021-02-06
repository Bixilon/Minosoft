package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.minosoft.data.assets.AssetsManager
import de.matthiasmann.twl.utils.PNGDecoder
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer


object TextureLoader {
    private val TRANSPARENT_TEXTURES = listOf("block/glass")
    fun loadTextureArray(assetsManager: AssetsManager, textures: List<Texture>): Map<Texture, ByteBuffer> {
        val result: MutableMap<Texture, ByteBuffer> = mutableMapOf()
        for (texture in textures) {
            result[texture] = loadTexture(assetsManager, texture)
        }
        return result.toMap()
    }

    private fun loadTexture(assetsManager: AssetsManager, texture: Texture): ByteBuffer {
        val decoder = PNGDecoder(assetsManager.readAssetAsStream("minecraft/textures/${texture.name}.png"))
        val buffer = BufferUtils.createByteBuffer(decoder.width * decoder.height * PNGDecoder.Format.RGBA.numComponents)
        decoder.decode(buffer, decoder.width * PNGDecoder.Format.RGBA.numComponents, PNGDecoder.Format.RGBA)
        buffer.flip()
        if (TRANSPARENT_TEXTURES.contains(texture.name)) { // ToDo: this should not be hardcoded!
            texture.isTransparent = true
        }
        buffer.flip()
        return buffer
    }
}
