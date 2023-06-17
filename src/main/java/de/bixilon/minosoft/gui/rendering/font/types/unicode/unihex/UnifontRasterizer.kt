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

class UnifontRasterizer(
    private val array: StaticTextureArray,
    totalWidth: Int,
) {
    private var width = totalWidth
    private var textures: MutableList<UnifontTexture> = mutableListOf()

    fun add(data: ByteArray): CodePointRenderer = add(data.size / (HEIGHT / Byte.SIZE_BITS), data)

    fun add(width: Int, data: ByteArray): CodePointRenderer {
        for (texture in textures) {
            texture.add(width, data)?.let { return it }
        }
        val renderer = createTexture().add(width, data)!!
        this.width -= width
        return renderer
    }

    private fun calculateRows(width: Int): Int {
        return 1024 / 16
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
