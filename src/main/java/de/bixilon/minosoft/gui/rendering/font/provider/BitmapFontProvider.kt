/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.font.provider

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.font.CharData
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.asList
import de.bixilon.minosoft.util.KUtil.toDouble
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import glm_.vec2.Vec2

class BitmapFontProvider(
    private val renderWindow: RenderWindow,
    data: Map<String, Any>,
) : FontProvider {
    val height = data["height"]?.toInt() ?: 8
    val ascent = data["ascent"].toDouble()
    private val chars: MutableMap<Char, CharData> = mutableMapOf()
    private val heightScale = Font.CHAR_HEIGHT.toFloat() / height

    init {
        val texture = renderWindow.textureManager.staticTextures.createTexture(data["file"].toResourceLocation().texture())
        texture.load(renderWindow.connection.assetsManager)
        val textureData = texture.data!!
        val pixel = Vec2(1.0f) / texture.size
        for ((y, row) in data["chars"].asList().withIndex()) {
            val xStart = IntArray(CHARS_PER_ROW) { 8 }
            val xEnd = IntArray(CHARS_PER_ROW) { 0 }
            val yStart = pixel.y * y * height
            val yEnd = pixel.y * (y + 1) * height


            for (i in 0 until height * CHAR_WIDTH * CHARS_PER_ROW) {
                val pixelRow = i % CHAR_WIDTH
                val charIndex = (i / CHAR_WIDTH) % CHARS_PER_ROW

                val alpha = textureData.get((y * height * CHAR_WIDTH * CHARS_PER_ROW + i) * 4 + 3)

                if (alpha == 0.toByte()) {
                    continue
                }
                // non transparent pixel
                if (xStart[charIndex] > pixelRow) {
                    xStart[charIndex] = pixelRow
                }
                if (xEnd[charIndex] < pixelRow) {
                    xEnd[charIndex] = pixelRow
                }
            }

            for (i in xEnd.indices) {
                xEnd[i]++
            }

            for ((x, char) in row.unsafeCast<String>().codePoints().toArray().withIndex()) {
                val xOffset = pixel.x * CHAR_WIDTH * x

                val uvStart = Vec2(
                    x = xOffset + (pixel.x * xStart[x]) - RenderConstants.UV_ADD,
                    y = yStart,
                )
                val uvEnd = Vec2(
                    x = xOffset + (pixel.x * xEnd[x]),
                    y = yEnd,
                )

                var width = xEnd[x] - xStart[x]

                if (width <= 0) {
                    width = EMPTY_CHAR_WIDTH
                }

                val scaledWidth = (width * heightScale).toInt()

                val charData = CharData(
                    renderWindow = renderWindow,
                    char = char.toChar(),
                    texture = texture,
                    width = width,
                    scaledWidth = scaledWidth,
                    uvStart = uvStart,
                    uvEnd = uvEnd,
                )

                chars[char.toChar()] = charData
            }
        }
        textureData.rewind()
    }

    override fun postInit() {
        for (char in chars.values) {
            char.postInit()
        }
    }

    override fun get(char: Char): CharData? {
        return chars[char]
    }

    companion object : FontProviderFactory<BitmapFontProvider> {
        private const val EMPTY_CHAR_WIDTH = 4
        private const val CHARS_PER_ROW = 16
        private const val CHAR_WIDTH = 8
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:bitmap".toResourceLocation()

        override fun build(renderWindow: RenderWindow, data: Map<String, Any>): BitmapFontProvider {
            return BitmapFontProvider(renderWindow, data)
        }
    }
}
