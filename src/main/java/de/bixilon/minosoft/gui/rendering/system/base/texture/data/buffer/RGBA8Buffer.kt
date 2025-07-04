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

import glm_.vec2.Vec2i
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

class RGBA8Buffer(
    override var size: Vec2i,
    override var data: ByteBuffer,
) : TextureBuffer {
    override val bits get() = 8
    override val bytes get() = 4
    override val components get() = 4
    override val alpha get() = true


    constructor(size: Vec2i, array: ByteArray) : this(size, ByteBuffer.wrap(array))
    constructor(size: Vec2i) : this(size, BufferUtils.createByteBuffer(size.x * size.y * 4))


    override fun setRGBA(x: Int, y: Int, red: Int, green: Int, blue: Int, alpha: Int) {
        val stride = stride(x, y)
        data.put(stride + 0, red.toByte())
        data.put(stride + 1, green.toByte())
        data.put(stride + 2, blue.toByte())
        data.put(stride + 3, alpha.toByte())
    }

    override fun setRGB(x: Int, y: Int, value: RGBColor) = setRGBA(x, y, value.red, value.green, value.blue, 0xFF)
    override fun setRGBA(x: Int, y: Int, value: RGBAColor) = setRGBA(x, y, value.red, value.green, value.blue, value.alpha)

    override fun copy() = RGBA8Buffer(Vec2i(size), data.duplicate())

    override fun create(size: Vec2i) = RGBA8Buffer(Vec2i(size))

    private operator fun get(index: Int): Int {
        return data[index].toInt() and 0xFF
    }

    override fun getRGBA(x: Int, y: Int): RGBAColor {
        val stride = stride(x, y)
        return RGBAColor(this[stride + 0], this[stride + 1], this[stride + 2], this[stride + 3])
    }

    override fun getRGB(x: Int, y: Int): RGBColor {
        val stride = stride(x, y)
        return RGBColor(this[stride + 0], this[stride + 1], this[stride + 2])
    }

    override fun getR(x: Int, y: Int) = this[stride(x, y) + 0]
    override fun getG(x: Int, y: Int) = this[stride(x, y) + 1]
    override fun getB(x: Int, y: Int) = this[stride(x, y) + 2]
    override fun getA(x: Int, y: Int) = this[stride(x, y) + 3]

    private fun stride(x: Int, y: Int): Int {
        if (x >= size.x || y >= size.y) throw IllegalArgumentException("Can not access pixel at ($x,$y), exceeds size: $size")
        return ((size.x * y) + x) * bytes
    }
}
