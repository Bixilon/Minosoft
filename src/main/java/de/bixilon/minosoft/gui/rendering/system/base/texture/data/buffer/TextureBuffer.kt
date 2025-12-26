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

package de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor.Companion.rgba
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import example.jonathan2520.SRGBAverager
import java.nio.ByteBuffer

interface TextureBuffer {
    val bits: Int
    val bytes: Int
    val components: Int
    val alpha: Boolean

    var data: ByteBuffer
    var size: Vec2i

    fun mipmap(): TextureBuffer {
        val target = create(size shr 1)

        for (y in 0 until target.size.y) {
            for (x in 0 until target.size.x) {
                val xOffset = x * 2
                val yOffset = y * 2

                val output = SRGBAverager.average(
                    getRGBA(xOffset + 0, yOffset + 0).rgba,
                    getRGBA(xOffset + 1, yOffset + 0).rgba,
                    getRGBA(xOffset + 0, yOffset + 1).rgba,
                    getRGBA(xOffset + 1, yOffset + 1).rgba,
                )

                target.setRGBA(x, y, output.rgba())
            }
        }

        return target
    }

    fun interpolate(a: TextureBuffer, b: TextureBuffer, progress: Float) {
        assert(a.size == b.size)
        assert(a.size == size)

        // TODO: optimize (specialize; simd)
        for (y in 0 until size.y) {
            for (x in 0 until size.x) {
                val a = a.getRGBA(x, y)
                val b = b.getRGBA(x, y)

                val destination = a.mix(b, progress)

                this.setRGBA(x, y, destination)
            }
        }
    }

    operator fun get(x: Int, y: Int) = getRGBA(x, y)

    fun getR(x: Int, y: Int): Int
    fun getG(x: Int, y: Int): Int
    fun getB(x: Int, y: Int): Int
    fun getA(x: Int, y: Int): Int

    fun getRGB(x: Int, y: Int): RGBColor

    fun getRGBA(x: Int, y: Int): RGBAColor
    fun setRGB(x: Int, y: Int, value: RGBColor)
    fun setRGBA(x: Int, y: Int, value: RGBAColor)
    fun setRGBA(x: Int, y: Int, red: Int, green: Int, blue: Int, alpha: Int)

    fun copy(): TextureBuffer
    fun create(size: Vec2i): TextureBuffer
    fun put(source: TextureBuffer, sourceOffset: Vec2i, targetOffset: Vec2i, size: Vec2i) {
        for (y in 0 until size.y) {
            for (x in 0 until size.x) {
                val rgba = source.getRGBA(sourceOffset.x + x, sourceOffset.y + y)
                setRGBA(targetOffset.x + x, targetOffset.y + y, rgba)
            }
        }
    }

    fun getTransparency(): TextureTransparencies {
        var transparency = TextureTransparencies.OPAQUE
        for (y in 0 until size.y) {
            for (x in 0 until size.x) {
                val alpha = getA(x, y)
                if (alpha == 0x00) {
                    transparency = TextureTransparencies.TRANSPARENT
                } else if (alpha < 0xFF) {
                    transparency = TextureTransparencies.TRANSLUCENT
                    break
                }
            }
        }
        return transparency
    }
}

