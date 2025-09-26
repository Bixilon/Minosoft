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
import de.bixilon.kutil.math.simple.FloatMath.clamp
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer

@JvmInline
value class PackedUV(val raw: FloatArray) {

    init {
        if (raw.size != SIZE) throw IllegalArgumentException("UV is not packed!")
    }

    companion object {
        const val BITS = 12
        const val MASK = (1 shl BITS) - 1

        const val COMPONENTS = Vec2f.LENGTH
        const val SIZE = 4


        inline fun pack(u: Float, v: Float): Float {
            val u = u.clamp(0.0f, 1.0f)
            val v = v.clamp(0.0f, 1.0f)

            val rU = (u * MASK).toInt()
            val rV = (v * MASK).toInt()

            val raw = (rU shl BITS) or (rV)

            return raw.buffer()
        }
    }
}
