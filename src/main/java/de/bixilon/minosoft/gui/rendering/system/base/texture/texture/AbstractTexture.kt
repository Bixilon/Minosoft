/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.base.texture.texture

import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureArray
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
import example.jonathan2520.SRGBAverager
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

interface AbstractTexture {
    val resourceLocation: ResourceLocation

    var textureArrayUV: Vec2
    var singlePixelSize: Vec2
    val state: TextureStates
    val size: Vec2i
    val transparency: TextureTransparencies
    var properties: ImageProperties

    var renderData: TextureRenderData

    var data: ByteBuffer?

    fun load(assetsManager: AssetsManager)


    fun generateMipMaps(): Array<ByteBuffer> {
        val images: MutableList<ByteBuffer> = mutableListOf()

        var data = data!!

        images += data

        for (i in 1 until OpenGLTextureArray.MAX_MIPMAP_LEVELS) {
            val mipMapSize = Vec2i(size.x shr i, size.y shr i)
            if (mipMapSize.x <= 0 || mipMapSize.y <= 0) {
                break
            }
            data = generateMipmap(data, Vec2i(size.x shr (i - 1), size.y shr (i - 1)))
            images += data
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

        buffer.rewind()
        return buffer
    }
}
