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

package de.bixilon.minosoft.gui.rendering.system.base.texture.data

import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer

open class MipmapTextureData(
    buffer: TextureBuffer,
    levels: Int,
) : TextureData(buffer) {
    val mipmaps: Array<TextureBuffer> = generate(levels)

    override fun collect(): Array<TextureBuffer> = mipmaps


    fun generate(levels: Int): Array<TextureBuffer> {
        val images: MutableList<TextureBuffer> = mutableListOf(buffer)

        var data = buffer
        for (i in 0 until levels) {
            val size = data.size shr 1
            if (size.x <= 0 || size.y <= 0) {
                break
            }
            data = data.mipmap()
            images += data
        }

        return images.toTypedArray()
    }
}
