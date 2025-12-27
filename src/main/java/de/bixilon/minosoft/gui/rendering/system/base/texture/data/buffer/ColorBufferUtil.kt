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

import de.bixilon.minosoft.util.collections.bytes.ByteListUtil.copy
import java.nio.ByteBuffer

object ColorBufferUtil {

    fun interpolate(a: ByteBuffer, b: ByteBuffer, destination: ByteBuffer, progress: Float) {
        assert(a.limit() == b.limit() && a.limit() == destination.limit())

        if (progress <= 0.0f) {
            a.copy(0, destination, 0, destination.limit())
            return
        }

        if (progress >= 1.0f) {
            b.copy(0, destination, 0, destination.limit())
            return
        }

        val multiplied = (progress * 256.0f).toInt()

        for (index in 0 until destination.limit()) {
            // TODO: simd
            val a = a[index].toInt() and 0xFF
            val b = b[index].toInt() and 0xFF


            val result = a + ((multiplied * (b - a)) shr 8)

            destination.put(index, result.toByte())
        }
    }
}
