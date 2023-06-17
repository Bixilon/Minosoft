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

import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.StaticTextureArray
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureArray.Companion.TEXTURE_RESOLUTION_ID_MAP

class UnifontRasterizer(
    private val array: StaticTextureArray,
    totalWidth: Int,
) {
    private var width = totalWidth
    private var textures: MutableList<UnifontTexture> = ArrayList(2)


    fun add(data: ByteArray): CodePointRenderer {
        val dataWith = data.size / (HEIGHT / Byte.SIZE_BITS)
        val iterator = textures.iterator()
        for (texture in iterator) {
            val code = texture.add(dataWith, data) ?: continue
            if (texture.remaining.last() <= 8) iterator.remove()
            this.width -= dataWith
            return code
        }
        val renderer = createTexture().add(dataWith, data)!!
        this.width -= dataWith
        return renderer
    }

    private fun calculateRows(width: Int): Int {
        var previous = TEXTURE_RESOLUTION_ID_MAP.last()
        for (index in TEXTURE_RESOLUTION_ID_MAP.size - 1 downTo 0) {
            val resolution = TEXTURE_RESOLUTION_ID_MAP[index]
            val size = resolution * resolution / HEIGHT
            if (width >= size) return previous / HEIGHT
            previous = resolution
        }

        return 1
    }

    private fun createTexture(): UnifontTexture {
        val texture = UnifontTexture(calculateRows(width))
        array.pushTexture(texture)
        this.textures += texture

        return texture
    }

    companion object {
        const val HEIGHT = 16
    }
}
