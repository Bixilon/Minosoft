package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.minosoft.data.assets.AssetsManager
import de.matthiasmann.twl.utils.PNGDecoder
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer


class Texture(
    val name: String,
    val id: Int,
) {
    var width: Int = 0
    var height: Int = 0
    var isTransparent: Boolean = false
    lateinit var buffer: ByteBuffer
    var loaded = false

    fun load(assetsManager: AssetsManager) {
        if (loaded) {
            return
        }
        val texturePath = if (name.endsWith(".png")) {
            name
        } else {
            "minecraft/textures/${name}.png"
        }
        val decoder = PNGDecoder(assetsManager.readAssetAsStream(texturePath))
        buffer = BufferUtils.createByteBuffer(decoder.width * decoder.height * PNGDecoder.Format.RGBA.numComponents)
        decoder.decode(buffer, decoder.width * PNGDecoder.Format.RGBA.numComponents, PNGDecoder.Format.RGBA)
        width = decoder.width
        height = decoder.height
        buffer.flip()
        if (TextureArray.TRANSPARENT_TEXTURES.contains(name)) { // ToDo: this should not be hardcoded!
            isTransparent = true
        }
        buffer.flip()
        loaded = true
    }
}
