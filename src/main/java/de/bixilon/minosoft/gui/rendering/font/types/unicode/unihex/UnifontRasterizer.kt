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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.types.empty.EmptyCodeRenderer
import de.bixilon.minosoft.gui.rendering.font.types.unicode.unihex.UnifontTexture.Companion.isPixelSet
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.StaticTextureArray

class UnifontRasterizer(
    private val array: StaticTextureArray,
    totalWidth: Int,
) {
    private var width = totalWidth
    private var textures: MutableList<UnifontTexture> = ArrayList(2)


    fun add(data: ByteArray): CodePointRenderer {
        val dataWidth = data.size / (HEIGHT / Byte.SIZE_BITS)

        val startEnd = getStartEnd(dataWidth, data)
        val start = startEnd ushr 16
        val end = startEnd and 0xFFFF
        if (end < start) return EmptyCodeRenderer()
        val width = end - start

        if (textures.isEmpty()) return forceAdd(dataWidth, width, start, end, data)
        val iterator = textures.iterator()
        for (texture in iterator) {
            val code = texture.add(dataWidth, width, start, end, data) ?: continue
            if (texture.remaining.last() <= 8) iterator.remove()
            this.width -= dataWidth
            return code
        }
        return forceAdd(dataWidth, width, start, end, data)
    }

    private fun forceAdd(dataWidth: Int, width: Int, start: Int, end: Int, data: ByteArray): CodePointRenderer {
        val renderer = createTexture().add(dataWidth, width, start, end, data)!!
        this.width -= dataWidth
        return renderer
    }

    private fun calculateRows(width: Int): Int {
        val size = array.findResolution(Vec2i(width, HEIGHT))
        return size.y / HEIGHT
    }

    private fun createTexture(): UnifontTexture {
        val texture = UnifontTexture(calculateRows(width))
        array += texture
        this.textures += texture

        return texture
    }

    private fun getStartEnd(width: Int, data: ByteArray): Int {
        var start = width
        var end = 0
        for (y in 0 until HEIGHT) {
            for (x in 0 until width) {
                val index = (y * width) + x
                if (!data.isPixelSet(index)) continue

                if (x < start) start = x
                if (x > end) end = x
            }
        }

        return (start shl 16) or (end + 1)
    }

    companion object {
        const val HEIGHT = 16
    }
}
