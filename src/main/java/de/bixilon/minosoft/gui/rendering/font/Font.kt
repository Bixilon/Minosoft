package de.bixilon.minosoft.gui.rendering.font

import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray

class Font {
    lateinit var providers: List<FontProvider>

    fun load(assetsManager: AssetsManager) {
        providers = FontLoader.loadFontProviders(assetsManager)
    }

    fun getChar(char: Char): FontChar {
        for (provider in providers) {
            provider.chars[char]?.let {
                return it
            }
        }
        throw IllegalStateException("$char can not be rendered!")
    }

    fun createAtlasTexture(): TextureArray {
        val textures: MutableList<Texture> = mutableListOf()
        for (provider in providers) {
            for (atlasPage in provider.atlasTextures) {
                textures.add(atlasPage)
            }
        }

        val textureArray = TextureArray.createTextureArray(textures = textures)


        val atlasWidthSinglePixel = 1f / textureArray.maxWidth
        val atlasHeightSinglePixel = 1f / textureArray.maxHeight

        for (provider in providers) {
            for (char in provider.chars.values) {
                char.calculateUV(provider.width, atlasWidthSinglePixel, atlasHeightSinglePixel)
            }
        }
        return textureArray
    }
}
