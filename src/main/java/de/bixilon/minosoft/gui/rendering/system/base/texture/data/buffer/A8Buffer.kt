/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
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
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import java.nio.ByteBuffer

class A8Buffer(
    override var size: Vec2i,
    override var data: ByteBuffer,
) : TextureBuffer {
    override val bits get() = 8
    override val bytes get() = 1
    override val components get() = 1
    override val alpha get() = true

    constructor(size: Vec2i, array: ByteArray) : this(size, ByteBuffer.wrap(array))
    constructor(size: Vec2i) : this(size, ByteBuffer.allocateDirect(size.x * size.y * 1))


    fun setA(x: Int, y: Int, alpha: Int) {
        data.put(offset(x, y), alpha.toByte())
    }

    override fun setRGBA(x: Int, y: Int, red: Int, green: Int, blue: Int, alpha: Int) {
        setA(x, y, alpha)
    }

    override fun setRGB(x: Int, y: Int, value: RGBColor) = Broken("No alpha!")
    override fun setRGBA(x: Int, y: Int, value: RGBAColor) = setA(x, y, value.alpha)

    override fun copy() = A8Buffer(size, ByteBuffer.allocateDirect(data.limit()).apply { put(data) })

    override fun create(size: Vec2i) = A8Buffer(size)

    private operator fun get(index: Int): Int {
        return data[index].toInt() and 0xFF
    }

    override fun getRGBA(x: Int, y: Int): RGBAColor {
        val offset = offset(x, y)
        val alpha = this[offset + 0]
        return RGBAColor(alpha, alpha, alpha, alpha)
    }

    override fun getRGB(x: Int, y: Int): RGBColor {
        val offset = offset(x, y)
        val alpha = this[offset + 0]
        return RGBColor(alpha, alpha, alpha)
    }


    override fun getR(x: Int, y: Int) = this[offset(x, y)]
    override fun getG(x: Int, y: Int) = this[offset(x, y)]
    override fun getB(x: Int, y: Int) = this[offset(x, y)]
    override fun getA(x: Int, y: Int) = this[offset(x, y)]


    private fun offset(x: Int, y: Int): Int {
        if (x >= size.x || y >= size.y) throw IllegalArgumentException("Can not access pixel at ($x,$y), exceeds size: $size")
        return ((size.x * y) + x) * bytes
    }

    override fun interpolate(a: TextureBuffer, b: TextureBuffer, progress: Float) {
        assert(a.size == b.size)
        assert(a.size == size)

        if (a !is A8Buffer || b !is A8Buffer) return super.interpolate(a, b, progress)

        ColorBufferUtil.interpolate(a.data, b.data, this.data, progress)
    }


    override fun getTransparency(): TextureTransparencies {
        var transparency = TextureTransparencies.OPAQUE
        for (index in 0 until size.x * size.y) {
            val alpha = this[index * components]
            if (alpha == 0x00) {
                transparency = TextureTransparencies.TRANSPARENT
            } else if (alpha < 0xFF) {
                transparency = TextureTransparencies.TRANSLUCENT
                break
            }
        }
        return transparency
    }
}
