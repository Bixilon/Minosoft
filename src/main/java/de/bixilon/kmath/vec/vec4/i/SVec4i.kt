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

package de.bixilon.kmath.vec.vec4.i

import de.bixilon.kmath.vec.VecUtil.assertVec
import de.bixilon.kmath.vec.vec3.i._Vec3i
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.DirectionVector
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.util.KUtil.format
import kotlin.math.sqrt

@JvmInline
value class SVec4i(val raw: Int) : _Vec4i {

    constructor() : this(0, 0, 0, 0)
    constructor(x: Int, y: Int, z: Int, w: Int) : this(((w and MASK_W) shl SHIFT_W) or ((z and MASK_Z) shl SHIFT_Z) or ((y and MASK_Y) shl SHIFT_Y) or ((x and MASK_X) shl SHIFT_X)) {
        assertVec(x, -MAX_X, MAX_X)
        assertVec(y, -MAX_Y, MAX_Y)
        assertVec(z, -MAX_Z, MAX_Z)
        assertVec(w, -MAX_W, MAX_W)
    }


    override inline val x: Int get() = (((raw ushr SHIFT_X) and MASK_X) shl (Int.SIZE_BITS - BITS_X)) shr (Int.SIZE_BITS - BITS_X)
    override inline val y: Int get() = (((raw ushr SHIFT_Y) and MASK_Y) shl (Int.SIZE_BITS - BITS_Y)) shr (Int.SIZE_BITS - BITS_Y)
    override inline val z: Int get() = (((raw ushr SHIFT_Z) and MASK_Z) shl (Int.SIZE_BITS - BITS_Z)) shr (Int.SIZE_BITS - BITS_Z)
    override inline val w: Int get() = (((raw ushr SHIFT_W) and MASK_W) shl (Int.SIZE_BITS - BITS_W)) shr (Int.SIZE_BITS - BITS_W)


    inline fun plusX(): SVec4i {
        assertVec(this.x < MAX_X)
        return SVec4i(raw + X * 1)
    }

    inline fun plusX(x: Int): SVec4i {
        assertVec(this.x + x, -MAX_X, MAX_X)
        return SVec4i(raw + X * x)
    }

    inline fun minusX(): SVec4i {
        assertVec(this.x > -MAX_X)
        return SVec4i(raw - X * 1)
    }

    inline fun plusY(): SVec4i {
        assertVec(this.y < MAX_Y)
        return SVec4i(raw + Y * 1)
    }

    inline fun plusY(y: Int): SVec4i {
        assertVec(this.y + y, -MAX_Y, MAX_Y)
        return SVec4i(raw + Y * y)
    }

    inline fun minusY(): SVec4i {
        assertVec(this.y > -MAX_Y)
        return SVec4i(raw - Y * 1)
    }

    inline fun plusZ(): SVec4i {
        assertVec(this.z < MAX_Z)
        return SVec4i(raw + Z * 1)
    }

    inline fun plusZ(z: Int): SVec4i {
        assertVec(this.z + z, -MAX_Z, MAX_Z)
        return SVec4i(raw + Z * z)
    }

    inline fun minusZ(): SVec4i {
        assertVec(this.z > -MAX_Z)
        return SVec4i(raw - Z * 1)
    }

    inline fun plusW(): SVec4i {
        assertVec(this.z < MAX_W)
        return SVec4i(raw + W * 1)
    }

    inline fun plusW(w: Int): SVec4i {
        assertVec(this.w + w, -MAX_W, MAX_W)
        return SVec4i(raw + W * w)
    }

    inline fun minusW(): SVec4i {
        assertVec(this.w > -MAX_W)
        return SVec4i(raw - W * 1)
    }

    inline fun with(x: Int = this.x, y: Int = this.y, z: Int = this.z, w: Int = this.w) = SVec4i(x, y, z, w)

    inline operator fun plus(value: Int) = SVec4i(this.x + value, this.y + value, this.z + value, this.w + value)
    inline operator fun minus(value: Int) = SVec4i(this.x - value, this.y - value, this.z - value, this.w - value)
    inline operator fun times(value: Int) = SVec4i(this.x * value, this.y * value, this.z * value, this.w * value)
    inline operator fun div(value: Int) = SVec4i(this.x / value, this.y / value, this.z / value, this.w * value)


    inline operator fun unaryMinus() = SVec4i(-this.x, -this.y, -this.z, -this.w)
    inline operator fun unaryPlus() = this

    inline operator fun plus(other: _Vec4i) = SVec4i(this.x + other.x, this.y + other.y, this.z + other.z, this.w + other.w)
    inline operator fun minus(other: _Vec4i) = SVec4i(this.x - other.x, this.y - other.y, this.z - other.z, this.w + other.w)

    inline infix fun and(mask: Int) = SVec4i(x and mask, y and mask, z and mask, w and mask)

    fun length2() = x * x + y * y + z * z + w * w
    fun length() = sqrt(length2().toFloat())


    override fun toString() = "(${this.x} ${this.y} ${this.z})"
    override fun toText() = BaseComponent().apply {
        this += "("
        this += x.format()
        this += " "
        this += y.format()
        this += " "
        this += z.format()
        this += " "
        this += w.format()
        this += ")"
    }


    companion object {
        const val BITS_X = 8
        const val MASK_X = (1 shl BITS_X) - 1
        const val SHIFT_X = 0

        const val BITS_Y = 8
        const val MASK_Y = (1 shl BITS_Y) - 1
        const val SHIFT_Y = BITS_X

        const val BITS_Z = 8
        const val MASK_Z = (1 shl BITS_Z) - 1
        const val SHIFT_Z = BITS_X + BITS_Y

        const val BITS_W = 8
        const val MASK_W = (1 shl BITS_W) - 1
        const val SHIFT_W = BITS_X + BITS_Y + BITS_Z

        const val X = 1 shl SHIFT_X
        const val Z = 1 shl SHIFT_Z
        const val Y = 1 shl SHIFT_Y
        const val W = 1 shl SHIFT_W

        const val MAX_X = (1 shl BITS_X - 1) - 1
        const val MAX_Y = (1 shl BITS_Y - 1) - 1
        const val MAX_Z = (1 shl BITS_Z - 1) - 1
        const val MAX_W = (1 shl BITS_W - 1) - 1


        val EMPTY = SVec4i(0, 0, 0, 0)
    }
}
