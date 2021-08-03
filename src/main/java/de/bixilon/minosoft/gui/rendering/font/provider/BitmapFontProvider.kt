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
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.font.CharData
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

    init {
        val texture = renderWindow.textureManager.staticTextures.createTexture(data["file"].toResourceLocation().texture())
        texture.load(renderWindow.connection.assetsManager)
        val pixel = Vec2(1.0f) / texture.size
        for ((y, row) in data["chars"].asList().withIndex()) {
            val yStart = pixel.y * y * height
            val yEnd = pixel.y * (y + 1) * height
            for ((x, char) in row.unsafeCast<String>().toCharArray().withIndex()) {
                val charXStart = 0 // ToDo: Calculate dynamically
                val charXEnd = CHAR_WIDTH // ToDo: Calculate dynamically

                val xOffset = pixel.x * CHAR_WIDTH * x

                val uvStart = Vec2(
                    x = xOffset + (pixel.x * charXStart),
                    y = yStart,
                )
                val uvEnd = Vec2(
                    x = xOffset + (pixel.x * charXEnd),
                    y = yEnd,
                )

                val charData = CharData(
                    char = char,
                    texture = texture,
                    width = charXEnd - charXStart,
                    uvStart = uvStart,
                    uvEnd = uvEnd,
                )

                chars[char] = charData
            }
        }
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
        private const val CHAR_WIDTH = 8
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:bitmap".toResourceLocation()

        override fun build(renderWindow: RenderWindow, data: Map<String, Any>): BitmapFontProvider {
            return BitmapFontProvider(renderWindow, data)
        }
    }
}
