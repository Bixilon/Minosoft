package de.bixilon.minosoft.gui.rendering.font

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.mappings.ModIdentifier
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import java.io.InputStream

object FontLoader {
    private const val FONT_ATLAS_SIZE = 16
    private const val UNICODE_CHARS_PER_PAGE = FONT_ATLAS_SIZE * FONT_ATLAS_SIZE
    private const val UNICODE_SIZE = 16
    private val MISSING_UNICODE_PAGES = listOf(0x08, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF, 0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xEE, 0xED, 0xEE, 0xEF, 0xF0, 0xF1, 0xF2, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8)

    private fun getCharArray(data: JsonArray): List<Char> {
        val ret: MutableList<Char> = mutableListOf()
        for (string in data) {
            ret.addAll(string.asString.toCharArray().toList())
        }
        return ret
    }

    private fun loadBitmapFontProvider(atlasPath: ModIdentifier, height: Int? = 8, ascent: Int, chars: List<Char>, assetsManager: AssetsManager, atlasOffset: Int): FontProvider {
        val width = if (ascent == 7) { // ToDo: Why?
            8
        } else {
            9
        }
        val provider = FontProvider(width)
        val atlasTexture = Texture((atlasPath.mod + "/textures/" + atlasPath.identifier), atlasOffset)
        atlasTexture.load(assetsManager)
        val height = height ?: atlasTexture.width / FONT_ATLAS_SIZE
        provider.atlasTextures.add(atlasTexture)
        val charsCoordinates: MutableList<MutableList<FontChar>> = mutableListOf() // ToDo: Remove this
        for ((i, char) in chars.withIndex()) {
            if (i % FONT_ATLAS_SIZE == 0) {
                charsCoordinates.add(mutableListOf())
            }
            val fontChar = FontChar(0, atlasOffset, i / FONT_ATLAS_SIZE, i % FONT_ATLAS_SIZE, width, 0, height)
            provider.chars[char] = fontChar
            charsCoordinates[i / FONT_ATLAS_SIZE].add(fontChar)
        }
        atlasTexture.buffer.rewind()
        // calculate start and endpixel for every char
        for (y in 0 until atlasTexture.height) {
            for (x in 0 until atlasTexture.width) {
                val color = RGBColor(atlasTexture.buffer.get(), atlasTexture.buffer.get(), atlasTexture.buffer.get(), atlasTexture.buffer.get())
                if (color.alpha == 0) {
                    continue
                }
                val fontChar = charsCoordinates[y / height][x / width]
                val pixel = x % width
                if (fontChar.startPixel > pixel) {
                    fontChar.startPixel = pixel
                }
                if (fontChar.endPixel <= pixel) {
                    fontChar.endPixel = pixel + 1
                }
            }
        }
        for ((_, fontChar) in provider.chars) {
            if (fontChar.startPixel == width && fontChar.endPixel == 0) {
                // invisible char (like a space)
                fontChar.startPixel = 0
                fontChar.endPixel = width / 2 // <- Divide by 2, else spaces look really big...
            }
            fontChar.width = fontChar.endPixel - fontChar.startPixel

        }
        atlasTexture.buffer.flip()
        return provider
    }


    private fun loadUnicodeFontProvider(template: ModIdentifier, sizes: InputStream, assetsManager: AssetsManager, atlasOffset: Int): FontProvider {
        val provider = FontProvider(UNICODE_SIZE)
        var i = 0
        lateinit var currentAtlasTexture: Texture
        while (sizes.available() > 0) {
            if (i % 256 == 0) {
                currentAtlasTexture = if (MISSING_UNICODE_PAGES.contains(i / UNICODE_CHARS_PER_PAGE)) {
                    // ToDo: Why is this texture missing in minecraft?
                    Texture(TextureArray.DEBUG_TEXTURE.name, i / UNICODE_CHARS_PER_PAGE)
                } else {
                    // new page (texture)
                    Texture((template.mod + "/textures/" + template.identifier).format("%02x".format(i / 256)), atlasOffset + (i / 256))
                }
                currentAtlasTexture.load(assetsManager)
                provider.atlasTextures.add(currentAtlasTexture)
            }
            val sizeByte = sizes.read()
            val fontChar = FontChar((i / UNICODE_CHARS_PER_PAGE), atlasOffset + (i / UNICODE_CHARS_PER_PAGE), (i % UNICODE_CHARS_PER_PAGE) / FONT_ATLAS_SIZE, (i % UNICODE_CHARS_PER_PAGE) % FONT_ATLAS_SIZE, (sizeByte shr 4) and 0x0F, (sizeByte and 0x0F) + 1, UNICODE_SIZE)
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
            "ttf" -> {
                TODO("True Type Fonts are not implemented yet")
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
