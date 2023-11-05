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
import java.io.InputStream
import javax.imageio.ImageIO

object TextureUtil {

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
            4 -> intArrayOf(0, 1, 2, 3)
            3 -> intArrayOf(0, 1, 2)
            else -> intArrayOf(0, 0, 0)
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
}
