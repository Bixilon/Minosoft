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

package de.bixilon.minosoft.data.direction

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.data.world.vec.vec3.i._Vec3i
import de.bixilon.minosoft.util.KUtil.format

@JvmInline
value class DirectionVector private constructor(val value: Int) : _Vec3i {
    constructor() : this(0)

    override inline val x: Int get() = Integer.signum((value and (MASK shl SHIFT_X)) shl (Int.SIZE_BITS - SHIFT_X - BITS))
    override inline val y: Int get() = Integer.signum((value and (MASK shl SHIFT_Y)) shl (Int.SIZE_BITS - SHIFT_Y - BITS))
    override inline val z: Int get() = Integer.signum((value and (MASK shl SHIFT_Z)) shl (Int.SIZE_BITS - SHIFT_Z - BITS))

    operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        Axes.Z -> z
    }


    fun with(direction: Directions): DirectionVector {
        val shift = (direction.axis.ordinal * BITS)
        val mask = MASK shl shift

        val without = value and mask.inv()

        val value = if (direction.negative) 0x02 else 0x01

        return DirectionVector(without or (value shl shift))
    }

    override inline operator fun component1() = x
    override inline operator fun component2() = y
    override inline operator fun component3() = z

    override fun toString() = "v($x $y $z)"

    companion object {
        const val BITS = 2
        const val MASK = (1 shl BITS) - 1

        const val SHIFT_X = 0
        const val SHIFT_Y = SHIFT_X + BITS
        const val SHIFT_Z = SHIFT_Y + BITS
    }
}
