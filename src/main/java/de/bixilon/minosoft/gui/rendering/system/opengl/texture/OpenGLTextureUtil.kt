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

package de.bixilon.minosoft.gui.rendering.system.opengl.texture

import de.bixilon.kotlinglm.vec2.Vec2i
import example.jonathan2520.SRGBAverager
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import java.nio.ByteBuffer

object OpenGLTextureUtil {
    const val MAX_MIPMAP_LEVELS = 5


    fun createTextureArray(): Int {
        val textureId = glGenTextures()
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT)
        // glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, MAX_MIPMAP_LEVELS - 1)

        return textureId
    }

    fun generateMipMaps(data: ByteBuffer, size: Vec2i): Array<ByteBuffer> {
        val images: MutableList<ByteBuffer> = mutableListOf()

        images += data

        var currentData = data
        for (i in 1 until MAX_MIPMAP_LEVELS) {
            val mipMapSize = Vec2i(size.x shr i, size.y shr i)
            if (mipMapSize.x <= 0 || mipMapSize.y <= 0) {
                break
            }
            currentData = generateMipmap(currentData, Vec2i(size.x shr (i - 1), size.y shr (i - 1)))
            images += currentData
        }

        return images.toTypedArray()
    }


    private fun generateMipmap(origin: ByteBuffer, oldSize: Vec2i): ByteBuffer {
        // No Vec2i: performance reasons
        val oldSizeX = oldSize.x
        val newSizeX = oldSizeX shr 1

        val buffer = BufferUtils.createByteBuffer(origin.capacity() shr 1)
        buffer.limit(buffer.capacity())

        fun getRGB(x: Int, y: Int): Int {
            return origin.getInt((y * oldSizeX + x) * 4)
        }

        fun setRGB(x: Int, y: Int, color: Int) {
            buffer.putInt((y * newSizeX + x) * 4, color)
        }

        for (y in 0 until (oldSize.y shr 1)) {
            for (x in 0 until newSizeX) {
                val xOffset = x * 2
                val yOffset = y * 2

                val output = SRGBAverager.average(
                    getRGB(xOffset + 0, yOffset + 0),
                    getRGB(xOffset + 1, yOffset + 0),
                    getRGB(xOffset + 0, yOffset + 1),
                    getRGB(xOffset + 1, yOffset + 1),
                )

                setRGB(x, y, output)
            }
        }

        origin.position(0)
        buffer.position(0)
        return buffer
    }
}
