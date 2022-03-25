/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.font.CharData
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec2.Vec2

class LegacyUnicodeFontProvider(
    private val renderWindow: RenderWindow,
    data: Map<String, Any>,
) : FontProvider {
    private val chars: Array<CharData?> = arrayOfNulls(1 shl Char.SIZE_BITS)

    init {
        val template = data["template"].unsafeCast<String>()
        val sizes = renderWindow.connection.assetsManager[data["sizes"].toResourceLocation()]

        var char = 0
        for (page in 0 until UNICODE_PAGES) {
            if (MISSING_UNICODE_PAGES.contains(page)) {
                // This page somehow does not exist, but we are fine with it
                // ToDo: Check if it really exist
                sizes.skip(UNICODE_PAGE_SIZE.toLong()) // skip the sizes to not mess up
                char += UNICODE_PAGE_SIZE
                continue
            }
            val texture = renderWindow.textureManager.staticTextures.createTexture(template.format("%02x".format(page)).toResourceLocation().texture(), mipmaps = false)
            for (y in 0 until UNICODE_PAGE_SIZE / CHAR_SIZE) {
                val yStart = PIXEL.y * y * CHAR_SIZE
                val yEnd = PIXEL.y * (y + 1) * CHAR_SIZE
                for (x in 0 until UNICODE_PAGE_SIZE / CHAR_SIZE) {
                    val widthByte = sizes.read()
                    val charXStart = ((widthByte shr 4) and 0x0F) - 1
                    val charXEnd = (widthByte and 0x0F) + 1

                    val xOffset = PIXEL.x * CHAR_SIZE * x

                    val uvStart = Vec2(
                        x = xOffset + (PIXEL.x * charXStart),
                        y = yStart,
                    )

                    val uvEnd = Vec2(
                        x = xOffset + (PIXEL.x * charXEnd),
                        y = yEnd,
                    )

                    val width = (charXEnd - charXStart)
                    val scaledWidth = (width * HEIGHT_SCALE).toInt()

                    val charData = CharData(
                        renderWindow = renderWindow,
                        char = char,
                        texture = texture,
                        width = width,
                        scaledWidth = scaledWidth,
                        uvStart = uvStart,
                        uvEnd = uvEnd,
                    )

                    chars[char] = charData
                    char++
                }
            }
        }
    }


    override fun postInit(latch: CountUpAndDownLatch) {
        for (char in chars) {
            char?.postInit()
        }
    }

    override fun get(char: Int): CharData? {
        return chars.getOrNull(char)
    }

    companion object : FontProviderFactory<LegacyUnicodeFontProvider> {
        private val MISSING_UNICODE_PAGES = listOf(0x08, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF, 0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xEE, 0xED, 0xEE, 0xEF, 0xF0, 0xF1, 0xF2, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8)
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:legacy_unicode".toResourceLocation()
        private const val UNICODE_PAGE_SIZE = 256
        private const val UNICODE_PAGES = 256
        private const val CHAR_SIZE = 16
        private const val HEIGHT_SCALE = Font.CHAR_HEIGHT.toFloat() / CHAR_SIZE
        private val PIXEL = Vec2(1.0f) / UNICODE_PAGE_SIZE

        override fun build(renderWindow: RenderWindow, data: Map<String, Any>): LegacyUnicodeFontProvider {
            return LegacyUnicodeFontProvider(renderWindow, data)
        }
    }
}
