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

package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.file.FileUtil.mkdirParent
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.identified.ResourceLocationUtil.extend
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGB8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGBA8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBufferFactory
import de.matthiasmann.twl.utils.PNGDecoder
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

object TextureUtil {
    private val COMPONENTS_4 = intArrayOf(0, 1, 2, 3)
    private val COMPONENTS_3 = intArrayOf(0, 1, 2)
    private val COMPONENTS_1 = intArrayOf(0, 0, 0)

    fun ResourceLocation.texture(): ResourceLocation {
        return this.extend(prefix = "textures/", suffix = ".png")
    }

    private fun InputStream.readTexture1(factory: TextureBufferFactory<*>?): TextureBuffer {
        val decoder = PNGDecoder(this)
        val size = Vec2i(decoder.width, decoder.height)
        val buffer = factory?.create(size) ?: when {
            decoder.hasAlphaChannel() && decoder.hasAlpha() -> RGBA8Buffer(size)
            //  else -> RGB8Buffer(size) // TODO: pngdecoder is broken
            else -> RGBA8Buffer(size)
        }
        val format = when {
            buffer.bits == 8 && buffer.components == 4 && buffer.alpha -> PNGDecoder.Format.RGBA
            buffer.bits == 8 && buffer.components == 3 -> PNGDecoder.Format.RGB
            else -> Broken("PNGDecoder can not read to $buffer")
        }
        decoder.decode(buffer.data, decoder.width * buffer.bytes, format)

        return buffer
    }

    private fun InputStream.readTexture2(factory: TextureBufferFactory<*>?): TextureBuffer {
        val image: BufferedImage = ImageIO.read(this)
        val size = Vec2i(image.width, image.height)
        val buffer = factory?.create(size) ?: when {
            image.raster.numBands == 3 -> RGB8Buffer(size)
            else -> RGBA8Buffer(size)
        }

        val samples = when (image.raster.numBands) {
            4 -> COMPONENTS_4
            3 -> COMPONENTS_3
            else -> COMPONENTS_1
        }

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                var rgba = RGBAColor(image.raster.getSample(x, y, samples[0]), image.raster.getSample(x, y, samples[1]), image.raster.getSample(x, y, samples[2]))

                if (samples.size > 3) {
                    rgba = rgba.with(alpha = image.raster.getSample(x, y, samples[3]))
                } else {
                    rgba = rgba.with(alpha = image.alphaRaster?.getSample(x, y, 0) ?: 0xFF)
                }
                buffer.setRGBA(x, y, rgba)
            }
        }

        return buffer
    }

    fun InputStream.readTexture(factory: TextureBufferFactory<*>? = null) = use {
        try {
            readTexture1(factory)
        } catch (exception: Throwable) {
            this.reset()
            readTexture2(factory)
        }
    }

    fun dump(file: File, buffer: TextureBuffer, alpha: Boolean, flipY: Boolean) {
        val bufferedImage = BufferedImage(buffer.size.x, buffer.size.y, if (alpha && buffer.alpha) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB)

        for (x in 0 until buffer.size.x) {
            for (y in 0 until buffer.size.y) {
                val rgba = buffer.getRGBA(x, y)

                val targetY = if (flipY) buffer.size.y - (y + 1) else y

                bufferedImage.setRGB(x, targetY, 0xFF shl 24 or rgba.rgb)
                if (alpha) {
                    bufferedImage.alphaRaster.setSample(x, targetY, 0, rgba.alpha)
                }
            }
        }

        file.mkdirParent()

        ImageIO.write(bufferedImage, "png", file)
    }

    fun RGBAColor.isBlack(): Boolean {
        if (alpha == 0x00) return true
        if (rgb == 0x00) return true

        return false
    }
}
