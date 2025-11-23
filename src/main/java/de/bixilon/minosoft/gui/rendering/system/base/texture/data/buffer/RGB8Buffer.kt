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
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import java.nio.ByteBuffer

class RGB8Buffer(
    override var size: Vec2i,
    override var data: ByteBuffer,
) : TextureBuffer {
    override val bits get() = 8
    override val bytes get() = 3
    override val components get() = 3
    override val alpha get() = true

    constructor(size: Vec2i, array: ByteArray) : this(size, ByteBuffer.wrap(array))
    constructor(size: Vec2i) : this(size, ByteBuffer.allocateDirect(size.x * size.y * 3))


    fun setRGB(x: Int, y: Int, red: Int, green: Int, blue: Int) {
        val offset = offset(x, y)
        data.put(offset + 0, red.toByte())
        data.put(offset + 1, green.toByte())
        data.put(offset + 2, blue.toByte())
    }

    override fun setRGBA(x: Int, y: Int, red: Int, green: Int, blue: Int, alpha: Int) {
        setRGB(x, y, red, green, blue)
    }

    override fun setRGB(x: Int, y: Int, value: RGBColor) = setRGB(x, y, value.red, value.green, value.blue)
    override fun setRGBA(x: Int, y: Int, value: RGBAColor) = setRGB(x, y, value.red, value.green, value.blue)

    override fun copy() = RGB8Buffer(size, data.duplicate())

    override fun create(size: Vec2i) = RGB8Buffer(size)

    private operator fun get(index: Int): Int {
        return data[index].toInt() and 0xFF
    }

    override fun getRGBA(x: Int, y: Int): RGBAColor {
        val offset = offset(x, y)
        return RGBAColor(this[offset + 0], this[offset + 1], this[offset + 2])
    }

    override fun getRGB(x: Int, y: Int): RGBColor {
        val offset = offset(x, y)
        return RGBColor(this[offset + 0], this[offset + 1], this[offset + 2])
    }


    override fun getR(x: Int, y: Int) = this[offset(x, y) + 0]
    override fun getG(x: Int, y: Int) = this[offset(x, y) + 1]
    override fun getB(x: Int, y: Int) = this[offset(x, y) + 2]
    override fun getA(x: Int, y: Int) = 0xFF


    private fun offset(x: Int, y: Int): Int {
        if (x >= size.x || y >= size.y) throw IllegalArgumentException("Can not access pixel at ($x,$y), exceeds size: $size")
        return ((size.x * y) + x) * bytes
    }

    override fun getTransparency() = TextureTransparencies.OPAQUE
}
