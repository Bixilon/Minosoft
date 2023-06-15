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

package de.bixilon.minosoft.gui.rendering.font.types.bitmap

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.types.PostInitFontType
import de.bixilon.minosoft.gui.rendering.font.types.empty.EmptyCodeRenderer
import de.bixilon.minosoft.gui.rendering.font.types.factory.FontTypeFactory
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.nio.ByteBuffer
import java.util.stream.IntStream

class BitmapFontType(
    val chars: Int2ObjectOpenHashMap<CodePointRenderer>,
) : PostInitFontType {

    init {
        chars.trim()
    }

    override fun get(codePoint: Int): CodePointRenderer? {
        return chars[codePoint]
    }

    override fun postInit(latch: AbstractLatch) {
        for (char in chars.values) {
            if (char !is BitmapCodeRenderer) continue
            char.updateArray()
        }
    }


    companion object : FontTypeFactory<BitmapFontType> {
        private const val ROW = 16
        override val identifier = minecraft("bitmap")

        override fun build(context: RenderContext, data: JsonObject): BitmapFontType? {
            val file = data["file"]?.toString()?.let { it.toResourceLocation().texture() } ?: throw IllegalArgumentException("Missing file!")
            val height = data["height"]?.toInt() ?: 8
            val ascent = data["ascent"]?.toInt() ?: 8
            val chars = data["chars"]?.listCast<String>() ?: throw IllegalArgumentException("Missing chars!")
            return load(file, height, ascent, chars, context)
        }

        private fun List<String>.codePoints(): Array<IntStream> {
            return this.map { it.codePoints() }.toTypedArray()
        }

        private fun load(file: ResourceLocation, height: Int, ascent: Int, chars: List<String>, context: RenderContext): BitmapFontType? {
            if (chars.isEmpty() || height <= 0) return null
            val texture = context.textureManager.staticTextures.createTexture(file)
            texture.load(context.connection.assetsManager) // force load it, we need to calculate the width of every char

            return load(texture, height, ascent, chars.codePoints())
        }

        private fun ByteBuffer.scanLine(y: Int, width: Int, start: IntArray, end: IntArray) {
            for (index in 0 until (width * ROW)) {
                val alpha = this[(((ROW * width) * y) + index) * 4 + 3].toInt()
                if (alpha == 0) {
                    // transparent
                    continue
                }

                val char = index / width
                val pixel = index % width

                start[char] = minOf(start[char], pixel)
                end[char] = maxOf(end[char], pixel)
            }
        }

        private fun createRenderer(texture: AbstractTexture, row: Int, column: Int, start: Int, end: Int, height: Int, ascent: Int): CodePointRenderer {
            if (end < start) return EmptyCodeRenderer()

            val uvStart = Vec2()
            val uvEnd = Vec2(0.1f)

            val width = 1.0f


            // TODO

            return BitmapCodeRenderer(texture, uvStart, uvEnd, width, ascent)
        }

        private fun load(texture: AbstractTexture, height: Int, ascent: Int, chars: Array<IntStream>): BitmapFontType? {
            val rows = chars.size
            val width = texture.size.x / ROW

            val start = IntArray(ROW) { width }
            val end = IntArray(ROW)

            val renderer = Int2ObjectOpenHashMap<CodePointRenderer>()

            for (row in 0 until rows) {
                val iterator = chars[row].iterator()

                for (pixel in 0 until height) {
                    texture.data!!.scanLine(row + pixel, width, start, end)
                }

                var column = 0
                while (iterator.hasNext()) {
                    val codePoint = iterator.nextInt()
                    renderer[codePoint] = createRenderer(texture, row, column, start[column], end[column], height, ascent)
                    column++
                }

                start.fill(width); end.fill(0) // fill with maximum values again
            }

            texture.data!!.rewind()

            if (renderer.isEmpty()) return null
            return BitmapFontType(renderer)
        }
    }
}
