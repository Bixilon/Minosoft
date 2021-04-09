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

package de.bixilon.minosoft.gui.rendering.font

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.textures.Texture
import java.io.InputStream

object FontLoader {
    private val FONT_JSON_RESOURCE_LOCATION = ResourceLocation("font/default.json")
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

    private fun loadBitmapFontProvider(atlasPath: ResourceLocation, height: Int? = 8, ascent: Int, chars: List<Char>, assetsManager: AssetsManager): FontProvider {
        val width = if (ascent == 7) { // ToDo: Why?
            8
        } else {
            9
        }
        val provider = FontProvider(width)
        val atlasTexture = Texture((Texture.getResourceTextureIdentifier(atlasPath.namespace, atlasPath.path)))
        atlasTexture.load(assetsManager)
        val height = height ?: atlasTexture.size.x / FONT_ATLAS_SIZE
        provider.atlasTextures.add(atlasTexture)
        val charsCoordinates: MutableList<MutableList<FontChar>> = mutableListOf() // ToDo: Remove this
        for ((i, char) in chars.withIndex()) {
            if (i % FONT_ATLAS_SIZE == 0) {
                charsCoordinates.add(mutableListOf())
            }
            val fontChar = FontChar(atlasTexture, i / FONT_ATLAS_SIZE, i % FONT_ATLAS_SIZE, width, 0, height)
            provider.chars[char] = fontChar
            charsCoordinates[i / FONT_ATLAS_SIZE].add(fontChar)
        }
        atlasTexture.buffer!!.rewind()
        // calculate start and endpixel for every char
        for (y in 0 until atlasTexture.size.y) {
            for (x in 0 until atlasTexture.size.x) {
                val color = RGBColor(atlasTexture.buffer!!.get(), atlasTexture.buffer!!.get(), atlasTexture.buffer!!.get(), atlasTexture.buffer!!.get())
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
            fontChar.size.x = fontChar.endPixel - fontChar.startPixel

        }
        atlasTexture.buffer!!.flip()
        return provider
    }


    private fun loadUnicodeFontProvider(template: ResourceLocation, sizes: InputStream, assetsManager: AssetsManager): FontProvider {
        val provider = FontProvider(UNICODE_SIZE)
        var i = 0
        lateinit var currentAtlasTexture: Texture
        while (sizes.available() > 0) {
            if (i % 256 == 0) {
                currentAtlasTexture = if (MISSING_UNICODE_PAGES.contains(i / UNICODE_CHARS_PER_PAGE)) {
                    // ToDo: Why is this texture missing in minecraft?
                    Texture(RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION)
                } else {
                    // new page (texture)
                    Texture(Texture.getResourceTextureIdentifier(template.namespace, template.path.format("%02x".format(i / 256))))
                }
                currentAtlasTexture.load(assetsManager)
                provider.atlasTextures.add(currentAtlasTexture)
            }
            val sizeByte = sizes.read()
            val fontChar = FontChar(currentAtlasTexture, (i % UNICODE_CHARS_PER_PAGE) / FONT_ATLAS_SIZE, (i % UNICODE_CHARS_PER_PAGE) % FONT_ATLAS_SIZE, (sizeByte shr 4) and 0x0F, (sizeByte and 0x0F) + 1, UNICODE_SIZE)
            provider.chars[i.toChar()] = fontChar
            i++
        }
        return provider
    }

    fun loadFontProvider(data: JsonObject, assetsManager: AssetsManager): FontProvider {
        return when (data["type"].asString) {
            "bitmap" -> {
                loadBitmapFontProvider(ResourceLocation(data["file"].asString), data["height"]?.asInt, data["ascent"].asInt, getCharArray(data["chars"].asJsonArray), assetsManager)
            }
            "legacy_unicode" -> {
                loadUnicodeFontProvider(ResourceLocation(data["template"].asString), assetsManager.readAssetAsStream(ResourceLocation(data["sizes"].asString)), assetsManager)
            }
            "ttf" -> {
                TODO("True Type Fonts are not implemented yet")
            }
            else -> throw IllegalArgumentException("${data["type"]} is not a valid font provider type!")
        }
    }


    fun loadFontProviders(assetsManager: AssetsManager): List<FontProvider> {
        val ret: MutableList<FontProvider> = mutableListOf()
        for (providerElement in assetsManager.readJsonAsset(FONT_JSON_RESOURCE_LOCATION).asJsonObject["providers"].asJsonArray) {
            val provider = loadFontProvider(providerElement.asJsonObject, assetsManager)
            ret.add(provider)
        }
        return ret
    }
}
