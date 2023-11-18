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

package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.file.FileUtil.createParent
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.identified.ResourceLocationUtil.extend
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.matthiasmann.twl.utils.PNGDecoder
import org.lwjgl.BufferUtils
import org.lwjgl.system.MemoryUtil
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO

object TextureUtil {
    private val COMPONENTS_4 = intArrayOf(0, 1, 2, 3)
    private val COMPONENTS_3 = intArrayOf(0, 1, 2)
    private val COMPONENTS_1 = intArrayOf(0, 0, 0)

    fun ResourceLocation.texture(): ResourceLocation {
        return this.extend(prefix = "textures/", suffix = ".png")
    }

    fun TextureTransparencies.getMesh(mesh: ChunkMeshes): ChunkMesh {
        return when (this) {
            TextureTransparencies.OPAQUE -> mesh.opaqueMesh
            TextureTransparencies.TRANSLUCENT -> mesh.translucentMesh
            TextureTransparencies.TRANSPARENT -> mesh.transparentMesh
        }!!
    }

    private fun InputStream.readTexture1(): TextureData {
        val decoder = PNGDecoder(this)
        val data = BufferUtils.createByteBuffer(decoder.width * decoder.height * PNGDecoder.Format.RGBA.numComponents)
        decoder.decode(data, decoder.width * PNGDecoder.Format.RGBA.numComponents, PNGDecoder.Format.RGBA)

        return TextureData(Vec2i(decoder.width, decoder.height), data)
    }

    private fun InputStream.readTexture2(): TextureData {
        val image: BufferedImage = ImageIO.read(this)

        val byteOutput = ByteArrayOutputStream()
        val dataOutput = DataOutputStream(byteOutput)

        val samples = when (image.raster.numBands) {
            4 -> COMPONENTS_4
            3 -> COMPONENTS_3
            else -> COMPONENTS_1
        }

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                dataOutput.writeByte(image.raster.getSample(x, y, samples[0]))
                dataOutput.writeByte(image.raster.getSample(x, y, samples[1]))
                dataOutput.writeByte(image.raster.getSample(x, y, samples[2]))
                if (samples.size > 3) {
                    dataOutput.writeByte(image.raster.getSample(x, y, samples[3]))
                } else {
                    val alpha = image.alphaRaster?.getSample(x, y, 0) ?: 0xFF
                    dataOutput.writeByte(alpha)
                }
            }
        }

        val buffer = MemoryUtil.memAlloc(byteOutput.size())
        buffer.put(byteOutput.toByteArray())

        return TextureData(Vec2i(image.width, image.height), buffer)
    }

    fun InputStream.readTexture(): TextureData {
        return try {
            readTexture1()
        } catch (exception: Throwable) {
            this.reset()
            readTexture2()
        }
    }

    fun dump(file: File, size: Vec2i, buffer: ByteBuffer, alpha: Boolean, flipY: Boolean) {
        val bufferedImage = BufferedImage(size.x, size.y, if (alpha) BufferedImage.TYPE_INT_ARGB else BufferedImage.TYPE_INT_RGB)
        val components = if (alpha) 4 else 3

        for (x in 0 until size.x) {
            for (y in 0 until size.y) {
                val index: Int = (x + size.x * y) * components
                val red: Int = buffer[index].toInt() and 0xFF
                val green: Int = buffer[index + 1].toInt() and 0xFF
                val blue: Int = buffer[index + 2].toInt() and 0xFF

                val targetY = if (flipY) size.y - (y + 1) else y

                bufferedImage.setRGB(x, targetY, 0xFF shl 24 or (red shl 16) or (green shl 8) or blue)
                if (alpha) {
                    val alpha = buffer[index + 3].toInt() and 0xFF
                    bufferedImage.alphaRaster.setSample(x, targetY, 0, alpha)
                }
            }
        }

        file.createParent()

        ImageIO.write(bufferedImage, "png", file)
    }

    fun copy(sourceOffset: Vec2i, source: TextureData, targetOffset: Vec2i, target: TextureData, size: Vec2i) {
        for (y in 0 until size.y) {
            for (x in 0 until size.x) {
                val sofs = ((sourceOffset.y + y) * source.size.x + (sourceOffset.x + x)) * 4
                val dofs = ((targetOffset.y + y) * target.size.x + (targetOffset.x + x)) * 4

                target.buffer.put(dofs + 0, source.buffer.get(sofs + 0))
                target.buffer.put(dofs + 1, source.buffer.get(sofs + 1))
                target.buffer.put(dofs + 2, source.buffer.get(sofs + 2))
                target.buffer.put(dofs + 3, source.buffer.get(sofs + 3))
            }
        }
    }
}
