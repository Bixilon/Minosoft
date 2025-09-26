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

package de.bixilon.kmath.vec.vec3.i

import de.bixilon.kmath.vec.VecUtil.assertVec
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.DirectionVector
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.util.KUtil.format

@JvmInline
value class SVec3i(val raw: Int) : _Vec3i {

    constructor() : this(0, 0, 0)
    constructor(x: Int, y: Int, z: Int) : this(((y and MASK_Y) shl SHIFT_Y) or ((z and MASK_Z) shl SHIFT_Z) or ((x and MASK_X) shl SHIFT_X)) {
        assertVec(x, -MAX_X, MAX_X)
        assertVec(y, -MAX_Y, MAX_Y)
        assertVec(z, -MAX_Z, MAX_Z)
    }


    override inline val x: Int get() = (((raw ushr SHIFT_X) and MASK_X) shl (Int.SIZE_BITS - BITS_X)) shr (Int.SIZE_BITS - BITS_X)
    override inline val y: Int get() = (((raw ushr SHIFT_Y) and MASK_Y) shl (Int.SIZE_BITS - BITS_Y)) shr (Int.SIZE_BITS - BITS_Y)
    override inline val z: Int get() = (((raw ushr SHIFT_Z) and MASK_Z) shl (Int.SIZE_BITS - BITS_Z)) shr (Int.SIZE_BITS - BITS_Z)


    inline fun plusX(): SVec3i {
        assertVec(this.x < MAX_X)
        return SVec3i(raw + X * 1)
    }

    inline fun plusX(x: Int): SVec3i {
        assertVec(this.x + x, -MAX_X, MAX_X)
        return SVec3i(raw + X * x)
    }

    inline fun minusX(): SVec3i {
        assertVec(this.x > -MAX_X)
        return SVec3i(raw - X * 1)
    }

    inline fun plusY(): SVec3i {
        assertVec(this.y < MAX_Y)
        return SVec3i(raw + Y * 1)
    }

    inline fun plusY(y: Int): SVec3i {
        assertVec(this.y + y, -MAX_Y, MAX_Y)
        return SVec3i(raw + Y * y)
    }

    inline fun minusY(): SVec3i {
        assertVec(this.y > -MAX_Y)
        return SVec3i(raw - Y * 1)
    }

    inline fun plusZ(): SVec3i {
        assertVec(this.z < MAX_Y)
        return SVec3i(raw + Z * 1)
    }

    inline fun plusZ(z: Int): SVec3i {
        assertVec(this.z + z, -MAX_Z, MAX_Z)
        return SVec3i(raw + Z * z)
    }

    inline fun minusZ(): SVec3i {
        assertVec(this.z > -MAX_Z)
        return SVec3i(raw - Z * 1)
    }

    inline fun with(x: Int = this.x, y: Int = this.y, z: Int = this.z) = SVec3i(x, y, z)

    inline operator fun plus(value: Int) = SVec3i(this.x + value, this.y + value, this.z + value)
    inline operator fun minus(value: Int) = SVec3i(this.x - value, this.y - value, this.z - value)
    inline operator fun times(value: Int) = SVec3i(this.x * value, this.y * value, this.z * value)
    inline operator fun div(value: Int) = SVec3i(this.x / value, this.y / value, this.z / value)


    inline operator fun unaryMinus() = SVec3i(-this.x, -this.y, -this.z)
    inline operator fun unaryPlus() = this

    inline operator fun plus(direction: Directions) = SVec3i(this.x + direction.vector.x, this.y + direction.vector.y, this.z + direction.vector.z)
    inline operator fun minus(direction: Directions) = SVec3i(this.x - direction.vector.x, this.y - direction.vector.y, this.z - direction.vector.z)

    inline infix fun and(mask: Int) = SVec3i(x and mask, y and mask, z and mask)

    inline operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        Axes.Z -> z
    }

    override fun toString() = "(${this.x} ${this.y} ${this.z})"
    override fun toText() = BaseComponent().apply {
        this += "("
        this += x.format()
        this += " "
        this += y.format()
        this += " "
        this += z.format()
        this += ")"
    }


    companion object {
        const val BITS_X = 10
        const val MASK_X = (1 shl BITS_X) - 1
        const val SHIFT_X = 0

        const val BITS_Z = 10
        const val MASK_Z = (1 shl BITS_Z) - 1
        const val SHIFT_Z = BITS_X

        const val BITS_Y = 10
        const val MASK_Y = (1 shl BITS_Y) - 1
        const val SHIFT_Y = BITS_X + BITS_Z

        const val X = 1 shl SHIFT_X
        const val Z = 1 shl SHIFT_Z
        const val Y = 1 shl SHIFT_Y

        const val MAX_X = (1 shl BITS_X - 1) - 1
        const val MAX_Y = (1 shl BITS_Y - 1) - 1
        const val MAX_Z = (1 shl BITS_Z - 1) - 1


        val EMPTY = SVec3i(0, 0, 0)


        operator fun invoke(vector: DirectionVector) = SVec3i(vector.x, vector.y, vector.z)
    }
}
