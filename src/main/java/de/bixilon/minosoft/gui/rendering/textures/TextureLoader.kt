package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.minosoft.data.assets.AssetsManager
import de.matthiasmann.twl.utils.PNGDecoder
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

object TextureLoader {
    fun loadTextureArray(assetsManager: AssetsManager, textures: Array<String>): Map<String, ByteBuffer> {
        val result: MutableMap<String, ByteBuffer> = mutableMapOf()
        for (texture in textures) {
            result[texture] = loadTexture(assetsManager, texture)
        }
        return result.toMap()
    }

    private fun loadTexture(assetsManager: AssetsManager, texture: String): ByteBuffer {
        val decoder = PNGDecoder(assetsManager.readAssetAsStream("minecraft/textures/${texture}.png"))
        val buffer = BufferUtils.createByteBuffer(decoder.width * decoder.height * 4)
        decoder.decode(buffer, decoder.width * 4, PNGDecoder.Format.RGBA)
        buffer.flip()
        return buffer
    }
}
