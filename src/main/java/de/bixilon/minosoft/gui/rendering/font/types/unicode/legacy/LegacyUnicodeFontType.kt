/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.font.types.unicode.legacy

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.primitive.IntUtil.toHex
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.renderer.FontProperties
import de.bixilon.minosoft.gui.rendering.font.types.FontType
import de.bixilon.minosoft.gui.rendering.font.types.factory.FontTypeFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.StaticTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.io.InputStream

class LegacyUnicodeFontType(
    val chars: Array<LegacyUnicodeCodeRenderer?>,
) : FontType {

    override fun postInit(latch: AbstractLatch) {
        for (char in chars) {
            char?.updateArray()
        }
    }

    override fun get(codePoint: Int): LegacyUnicodeCodeRenderer? {
        return chars.getOrNull(codePoint)
    }

    companion object : FontTypeFactory<LegacyUnicodeFontType> {
        override val identifier = minecraft("legacy_unicode")
        private const val UNICODE_PAGES = 0xFF
        private const val PAGE_SIZE = 0xFF
        private const val CHAR_ROW = 0x0F
        private const val CHAR_SIZE = 0x0F
        private const val PIXEL = 1.0f / (CHAR_SIZE * CHAR_ROW)
        private const val WIDTH_SCALE = FontProperties.CHAR_HEIGHT / CHAR_SIZE.toFloat()

        override fun build(context: RenderContext, data: Map<String, Any>): LegacyUnicodeFontType? {
            val assets = context.connection.assetsManager
            val template = data["template"]?.toString() ?: throw IllegalArgumentException("template missing!")
            val sizes = data.loadSizes(assets) ?: return null

            return load(template, sizes, assets, context.textureManager.staticTextures)
        }

        private fun String.formatTemplate(page: Int): ResourceLocation {
            return this.replace("%s", page.toHex(2)).toResourceLocation().texture()
        }

        fun load(template: String, sizes: InputStream, assets: AssetsManager, textures: StaticTextureArray): LegacyUnicodeFontType {
            val chars: Array<LegacyUnicodeCodeRenderer?> = arrayOfNulls(1 shl Char.SIZE_BITS)
            for (pageId in 0 until UNICODE_PAGES) {
                val textureFile = template.formatTemplate(pageId)
                tryLoadPage(pageId, textureFile, chars, sizes, assets, textures)
            }

            return LegacyUnicodeFontType(chars)
        }

        private fun tryLoadPage(pageId: Int, textureFile: ResourceLocation, chars: Array<LegacyUnicodeCodeRenderer?>, sizes: InputStream, assets: AssetsManager, textures: StaticTextureArray) {
            if (textureFile !in assets) {
                // file not present, skip entire page
                sizes.skip(PAGE_SIZE.toLong())
                return
            }
            val texture = textures.createTexture(textureFile, mipmaps = false)

            loadPage(pageId, texture, chars, sizes)
        }

        private fun loadPage(pageId: Int, texture: AbstractTexture, chars: Array<LegacyUnicodeCodeRenderer?>, sizes: InputStream) {
            for (y in 0 until CHAR_ROW) {
                val yStart = (y * CHAR_SIZE) * PIXEL
                val yEnd = ((y + 1) * CHAR_SIZE) * PIXEL

                for (x in 0 until CHAR_ROW) {
                    val widthByte = sizes.read()
                    if (widthByte < 0) throw IllegalStateException("Unexpected end of sizes stream!")

                    val xStart = ((widthByte shr 4) and 0x0F) - 1
                    val width = ((widthByte and 0x0F) + 1) - xStart


                    val xOffset = (CHAR_SIZE * x) + xStart

                    val uvStart = Vec2(
                        x = xOffset * PIXEL,
                        y = yStart,
                    )

                    val uvEnd = Vec2(
                        x = xOffset + width * PIXEL,
                        y = yEnd,
                    )

                    val scaledWidth = width * WIDTH_SCALE

                    chars[(pageId shl 8) or (y shl 4) or x] = LegacyUnicodeCodeRenderer(texture, uvStart, uvEnd, scaledWidth)
                }
            }
        }

        private fun JsonObject.loadSizes(assets: AssetsManager): InputStream? {
            val file = this["sizes"]?.toString() ?: throw IllegalArgumentException("sizes missing!")
            val stream = assets[file.toResourceLocation()]

            if (stream.available() <= 0) return null
            return stream
        }
    }
}
