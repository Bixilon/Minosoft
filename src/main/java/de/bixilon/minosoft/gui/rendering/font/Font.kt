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

import de.bixilon.minosoft.data.assets.MinecraftAssetsManager
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray

class Font {
    lateinit var providers: List<FontProvider>
    val charHeight = 8

    fun load(assetsManager: MinecraftAssetsManager) {
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
                char.calculateUV(provider.width, atlasWidthSinglePixel, atlasHeightSinglePixel) // ToDo: Unicode: With should pe plus 1
            }
        }
        return textureArray
    }
}
