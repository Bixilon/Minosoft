/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.bit.BitByte.isBit
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor.Companion.rgba
import de.bixilon.minosoft.gui.rendering.RenderUtil.fixUVEnd
import de.bixilon.minosoft.gui.rendering.RenderUtil.fixUVStart
import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FontProperties
import de.bixilon.minosoft.gui.rendering.font.types.unicode.UnicodeCodeRenderer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGBA8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.loader.MemoryLoader
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture

class UnifontTexture(
    val rows: Int,
) {
    private val resolution = rows * UnifontRasterizer.HEIGHT
    val size: Vec2i = Vec2i(resolution)
    private val pixel = 1.0f / size.x

    val buffer = RGBA8Buffer(size)
    val texture = Texture(MemoryLoader { buffer }, 0)

    val remaining = IntArray(rows) { resolution }
    var totalRemaining = resolution * rows


    fun add(dataWidth: Int, width: Int, start: Int, end: Int, data: ByteArray): CodePointRenderer? {
        for ((index, remaining) in remaining.withIndex()) {
            if (remaining < width) continue
            this.remaining[index] = remaining - width
            totalRemaining -= width
            return rasterize(index, resolution - remaining, start, end, dataWidth, data)
        }

        return null
    }

    private fun TextureBuffer.set(row: Int, offset: Int, x: Int, y: Int) {
        setRGBA(offset + x, (row * UnifontRasterizer.HEIGHT + y), 0xFFFFFFFF.toInt().rgba())
    }

    private fun rasterize(row: Int, offset: Int, start: Int, end: Int, dataWidth: Int, data: ByteArray): CodePointRenderer {
        for (y in 0 until UnifontRasterizer.HEIGHT) {
            for (x in start until end) {
                val index = (y * dataWidth) + x
                if (!data.isPixelSet(index)) continue
                this.buffer.set(row, offset, x - start, y)
            }
        }

        val uvStart = Vec2f(pixel * (offset), pixel * (row * UnifontRasterizer.HEIGHT)).unsafe.apply { fixUVStart() }.unsafe
        val uvEnd = Vec2f(pixel * (offset + (end - start)), pixel * ((row + 1) * UnifontRasterizer.HEIGHT)).unsafe.apply { fixUVEnd() }.unsafe
        val width = (end - start) * (FontProperties.CHAR_BASE_HEIGHT.toFloat() / UnifontRasterizer.HEIGHT)

        return UnicodeCodeRenderer(texture, uvStart, uvEnd, width)
    }

    companion object {
        fun ByteArray.isPixelSet(index: Int): Boolean {
            val byte = this[index / Byte.SIZE_BITS].toInt()
            return byte.isBit(7 - (index % Byte.SIZE_BITS))
        }
    }
}
