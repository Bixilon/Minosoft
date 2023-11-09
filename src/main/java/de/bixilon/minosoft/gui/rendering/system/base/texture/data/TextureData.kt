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

package de.bixilon.minosoft.gui.rendering.system.base.texture.data

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.memory.TextureGenerator
import org.objenesis.ObjenesisStd
import java.nio.ByteBuffer

open class TextureData(
    val size: Vec2i,
    val buffer: ByteBuffer = TextureGenerator.allocate(size),
) {
    constructor(size: Vec2i, array: ByteArray) : this(size, ByteBuffer.wrap(array))

    open fun collect(): Array<ByteBuffer> = arrayOf(buffer)


    private operator fun get(offset: Int): Int {
        return buffer[offset].toInt() and 0xFF
    }

    operator fun get(x: Int, y: Int): Int {
        val offset = ((size.x * y) + x) * 4
        return (this[offset + 0] shl 24) or (this[offset + 1] shl 16) or (this[offset + 2] shl 8) or (this[offset + 3] shl 0)
    }

    companion object {
        val NULL = ObjenesisStd().newInstance(TextureData::class.java)
    }
}
