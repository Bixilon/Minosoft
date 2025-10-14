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

package de.bixilon.minosoft.gui.rendering.util.mesh.uv

import de.bixilon.kmath.vec.vec2.f.Vec2f

@JvmInline
value class UnpackedUV(val raw: FloatArray) {

    init {
        if (raw.size != SIZE) throw IllegalArgumentException("UV is not packed!")
    }

    fun pack(): PackedUV {
        val packed = FloatArray(PackedUV.SIZE)
        for (i in 0 until PackedUV.SIZE) {
            val u = raw[i * COMPONENT + 0]
            val v = raw[i * COMPONENT + 1]

            packed[i] = PackedUV.pack(u, v)
        }
        return PackedUV(packed)
    }

    companion object {
        const val COMPONENT = Vec2f.LENGTH
        const val COMPONENT_SIZE = 4
        const val SIZE = COMPONENT * COMPONENT_SIZE


        inline fun unpackU(uv: Float): Float {
            val raw = uv.toBits()
            return ((raw shr PackedUV.BITS) and PackedUV.MASK).toFloat() / PackedUV.MASK
        }

        inline fun unpackV(uv: Float): Float {
            val raw = uv.toBits()
            return ((raw shr 0) and PackedUV.MASK).toFloat() / PackedUV.MASK
        }
    }
}
