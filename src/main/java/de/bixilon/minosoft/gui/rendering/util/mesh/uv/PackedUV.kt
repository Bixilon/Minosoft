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
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.math.simple.FloatMath.clamp
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer

@JvmInline
value class PackedUV(val raw: Float) {
    val u: Float get() = ((raw.toBits() shr 1 * BITS) and MASK).toFloat() / MASK
    val v: Float get() = ((raw.toBits() shr 0 * BITS) and MASK).toFloat() / MASK


    constructor(u: Float, v: Float) : this(pack(u, v))
    constructor(uv: Vec2f) : this(pack(uv.x, uv.y))

    @Deprecated("nothing", level = DeprecationLevel.ERROR)
    fun copy(): PackedUV = Broken()
    fun copy(u: Float = this.u, v: Float = this.v) = PackedUV(u, v)

    companion object {
        const val BITS = 12
        const val MASK = (1 shl BITS) - 1

        val ZERO = PackedUV(0.0f, 0.0f)
        val ONE = PackedUV(1.0f, 1.0f)


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
