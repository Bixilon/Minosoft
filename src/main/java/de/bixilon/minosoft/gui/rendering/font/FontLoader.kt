package de.bixilon.minosoft.gui.rendering.font

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.mappings.ModIdentifier
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import java.io.InputStream

object FontLoader {
    private const val FONT_ATLAS_SIZE = 16
    private val MISSING_UNICODE_PAGES = listOf(0x08, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF, 0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xEE, 0xED, 0xEE, 0xEF, 0xF0, 0xF1, 0xF2, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8)

    private fun getCharArray(data: JsonArray): List<Char> {
        val ret: MutableList<Char> = mutableListOf()
        for (string in data) {
            ret.addAll(string.asString.toCharArray().toList())
        }
        return ret
    }

    private fun loadBitmapFontProvider(atlasPath: ModIdentifier, height: Int? = 8, ascent: Int, chars: List<Char>, assetsManager: AssetsManager, atlasOffset: Int): FontProvider {
        val width = if (ascent == 7) {
            8
        } else {
            9
        }
        val provider = FontProvider(width)
        val atlasTexture = Texture((atlasPath.mod + "/textures/" + atlasPath.identifier), atlasOffset)
        atlasTexture.load(assetsManager)
        provider.atlasTextures.add(atlasTexture)
        val height = height ?: atlasTexture.width / 16
        for ((i, char) in chars.withIndex()) {
            val fontChar = FontChar(0, atlasOffset, i / FONT_ATLAS_SIZE, i % FONT_ATLAS_SIZE, 0, width, height)
            provider.chars[char] = fontChar
        }
        return provider
    }


    private fun loadUnicodeFontProvider(template: ModIdentifier, sizes: InputStream, assetsManager: AssetsManager, atlasOffset: Int): FontProvider {
        val provider = FontProvider(16)
        var i = 0
        lateinit var currentAtlasTexture: Texture
        while (sizes.available() > 0) {
            if (i % 256 == 0) {
                currentAtlasTexture = if (MISSING_UNICODE_PAGES.contains(i / 256)) {
                    // ToDo: Why is this texture missing in minecraft?
                    Texture(TextureArray.DEBUG_TEXTURE.name, i / 256)
                } else {
                    // new page (texture)
                    Texture((template.mod + "/textures/" + template.identifier).format("%02x".format(i / 256)), atlasOffset + (i / 256))
                }
                currentAtlasTexture.load(assetsManager)
                provider.atlasTextures.add(currentAtlasTexture)
            }
            val sizeByte = sizes.read()
            val fontChar = FontChar((i / 256), atlasOffset + (i / 256), (i % 256) / FONT_ATLAS_SIZE, (i % 256) % FONT_ATLAS_SIZE, (sizeByte shr 4) and 0x0F, sizeByte and 0x0F, 16)
            provider.chars[i.toChar()] = fontChar
            i++
        }
        return provider
    }

    fun loadFontProvider(data: JsonObject, assetsManager: AssetsManager, atlasTextureOffset: Int): FontProvider {
        return when (data["type"].asString) {
            "bitmap" -> {
                loadBitmapFontProvider(ModIdentifier(data["file"].asString), data["height"]?.asInt, data["ascent"].asInt, getCharArray(data["chars"].asJsonArray), assetsManager, atlasTextureOffset)
            }
            "legacy_unicode" -> {
                loadUnicodeFontProvider(ModIdentifier(data["template"].asString), assetsManager.readAssetAsStream(ModIdentifier(data["sizes"].asString)), assetsManager, atlasTextureOffset)
            }
            else -> throw IllegalArgumentException("${data["type"]} is not a valid font provider type!")
        }
    }


    fun loadFontProviders(assetsManager: AssetsManager): List<FontProvider> {
        val ret: MutableList<FontProvider> = mutableListOf()
        var atlasTextureOffset = 0
        for (providerElement in assetsManager.readJsonAsset("minecraft/font/default.json").asJsonObject["providers"].asJsonArray) {
            val provider = loadFontProvider(providerElement.asJsonObject, assetsManager, atlasTextureOffset)
            atlasTextureOffset += provider.atlasTextures.size
            ret.add(provider)
        }
        return ret
    }
}
