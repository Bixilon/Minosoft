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

package de.bixilon.minosoft.gui.rendering.font.types.unicode.unihex

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.bit.BitByte.isBit
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FontProperties
import de.bixilon.minosoft.gui.rendering.font.types.empty.EmptyCodeRenderer
import de.bixilon.minosoft.gui.rendering.font.types.unicode.UnicodeCodeRenderer
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.TextureRenderData
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties

class UnifontTexture(
    val rows: Int,
) : Texture {
    override val size: Vec2i = Vec2i(rows * UnifontRasterizer.HEIGHT)
    private val pixel = 1.0f / size.x
    override val transparency: TextureTransparencies = TextureTransparencies.TRANSPARENT
    override val mipmaps: Boolean get() = false

    override lateinit var array: TextureArrayProperties
    override lateinit var renderData: TextureRenderData
    override var data: TextureData = TextureData(size)
    override var properties = ImageProperties.DEFAULT
    override val state: TextureStates = TextureStates.LOADED

    private val remaining = IntArray(rows) { size.x }
    var totalRemaining = size.x * rows

    override fun load(context: RenderContext) = Unit

    fun add(width: Int, data: ByteArray): CodePointRenderer? {
        for ((index, remaining) in remaining.withIndex()) {
            if (remaining < width) continue
            this.remaining[index] = remaining - width
            totalRemaining -= width
            return rasterize(index, size.x - remaining, width, data)
        }

        return null
    }

    private fun TextureData.set(row: Int, offset: Int, x: Int, y: Int) {
        val index = ((row * UnifontRasterizer.HEIGHT + y) * size.x + offset + x) * 4

        buffer.putInt(index, 0xFFFFFFFF.toInt())
    }

    private fun rasterize(row: Int, offset: Int, width: Int, data: ByteArray): CodePointRenderer {
        var start = width
        var end = 0

        for (y in 0 until UnifontRasterizer.HEIGHT) {
            for (x in 0 until width) {
                val index = (y * width) + x
                if (!data.isPixelSet(index)) continue

                start = minOf(start, x)
                end = maxOf(end, x)

                this.data.set(row, offset, x, y)
            }
        }
        if (end < start) return EmptyCodeRenderer()
        end += 1

        val uvStart = Vec2(pixel * (offset + start), pixel * (row * UnifontRasterizer.HEIGHT))
        val uvEnd = Vec2(pixel * (offset + end), pixel * ((row + 1) * UnifontRasterizer.HEIGHT))
        val width = (end - start) * (FontProperties.CHAR_BASE_HEIGHT.toFloat() / UnifontRasterizer.HEIGHT)

        return UnicodeCodeRenderer(this, uvStart, uvEnd, width)
    }

    private fun ByteArray.isPixelSet(index: Int): Boolean {
        val byte = this[index / Byte.SIZE_BITS].toInt()
        return byte.isBit(7 - (index % Byte.SIZE_BITS))
    }
}
