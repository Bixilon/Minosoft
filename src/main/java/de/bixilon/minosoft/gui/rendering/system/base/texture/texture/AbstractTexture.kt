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

import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureArray
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
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

    var renderData: TextureRenderData?

    var data: ByteBuffer?

    fun load(assetsManager: AssetsManager)


    fun generateMipMaps(): Array<Pair<Vec2i, ByteBuffer>> {
        val ret: MutableList<Pair<Vec2i, ByteBuffer>> = mutableListOf()
        var lastBuffer = data!!
        var lastSize = size
        for (i in 0 until OpenGLTextureArray.MAX_MIPMAP_LEVELS) {
            val size = Vec2i(size.x shr i, size.y shr i)
            if (i != 0 && size.x != 0 && size.y != 0) {
                lastBuffer = generateMipmap(lastBuffer, lastSize, size)
                lastSize = size
            }
            ret += Pair(size, lastBuffer)
        }

        return ret.toTypedArray()
    }


    private fun ByteBuffer.getRGB(start: Int): RGBColor {
        return RGBColor(get(start), get(start + 1), get(start + 2), get(start + 3))
    }

    private fun ByteBuffer.setRGB(start: Int, color: RGBColor) {
        put(start, color.red.toByte())
        put(start + 1, color.green.toByte())
        put(start + 2, color.blue.toByte())
        put(start + 3, color.alpha.toByte())
    }

    @Deprecated(message = "This is garbage, will be improved soon...")
    private fun generateMipmap(biggerBuffer: ByteBuffer, oldSize: Vec2i, newSize: Vec2i): ByteBuffer {
        val sizeFactor = oldSize / newSize
        val buffer = BufferUtils.createByteBuffer(biggerBuffer.capacity() shr 1)
        buffer.limit(buffer.capacity())

        fun getRGB(x: Int, y: Int): RGBColor {
            return biggerBuffer.getRGB((y * oldSize.x + x) * 4)
        }

        fun setRGB(x: Int, y: Int, color: RGBColor) {
            buffer.setRGB((y * newSize.x + x) * 4, color)
        }

        for (y in 0 until newSize.y) {
            for (x in 0 until newSize.x) {

                // check what is the most used transparency
                val transparencyPixelCount = IntArray(TextureTransparencies.VALUES.size)
                for (mixY in 0 until sizeFactor.y) {
                    for (mixX in 0 until sizeFactor.x) {
                        val color = getRGB(x * sizeFactor.x + mixX, y * sizeFactor.y + mixY)
                        when (color.alpha) {
                            255 -> transparencyPixelCount[TextureTransparencies.OPAQUE.ordinal]++
                            0 -> transparencyPixelCount[TextureTransparencies.TRANSPARENT.ordinal]++
                            else -> transparencyPixelCount[TextureTransparencies.TRANSLUCENT.ordinal]++
                        }
                    }
                }
                var largest = 0
                for (count in transparencyPixelCount) {
                    if (count > largest) {
                        largest = count
                    }
                }
                var transparency: TextureTransparencies = TextureTransparencies.OPAQUE
                for ((index, count) in transparencyPixelCount.withIndex()) {
                    if (count >= largest) {
                        transparency = TextureTransparencies[index]
                        break
                    }
                }

                var count = 0
                var red = 0
                var green = 0
                var blue = 0
                var alpha = 0

                // make magic for the most used transparency
                for (mixY in 0 until sizeFactor.y) {
                    for (mixX in 0 until sizeFactor.x) {
                        val color = getRGB(x * sizeFactor.x + mixX, y * sizeFactor.y + mixY)
                        when (transparency) {
                            TextureTransparencies.OPAQUE -> {
                                if (color.alpha != 0xFF) {
                                    continue
                                }
                                red += color.red
                                green += color.green
                                blue += color.blue
                                alpha += color.alpha
                                count++
                            }
                            TextureTransparencies.TRANSPARENT -> {
                            }
                            TextureTransparencies.TRANSLUCENT -> {
                                red += color.red
                                green += color.green
                                blue += color.blue
                                alpha += color.alpha
                                count++
                            }
                        }
                    }
                }





                if (count == 0) {
                    count++
                }
                setRGB(x, y, RGBColor(red / count, green / count, blue / count, alpha / count))
            }
        }

        buffer.rewind()
        return buffer
    }
}
