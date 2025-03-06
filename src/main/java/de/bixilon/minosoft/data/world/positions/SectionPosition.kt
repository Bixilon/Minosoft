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

package de.bixilon.minosoft.data.world.positions

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.data.world.positions.BlockPositionUtil.assertPosition
import de.bixilon.minosoft.util.KUtil.format

@JvmInline
value class SectionPosition(
    inline val raw: Long,
) : TextFormattable {

    constructor() : this(0, 0, 0)

    constructor(x: Int, y: SectionHeight, z: Int) : this(((y and MASK_Y).toLong() shl SHIFT_Y) or ((z and MASK_Z).toLong() shl SHIFT_Z) or ((x and MASK_X).toLong() shl SHIFT_X)) {
        assertPosition(x, -MAX_X, MAX_X)
        assertPosition(y, MIN_Y, MAX_Y)
        assertPosition(z, -MAX_Z, MAX_Z)
    }

    inline val x: Int get() = (((raw ushr SHIFT_X).toInt() and MASK_X) shl (Int.SIZE_BITS - BITS_X)) shr (Int.SIZE_BITS - BITS_X)
    inline val y: SectionHeight get() = (((raw ushr SHIFT_Y).toInt() and MASK_Y) shl (Int.SIZE_BITS - BITS_Y)) shr (Int.SIZE_BITS - BITS_Y)
    inline val z: Int get() = (((raw ushr SHIFT_Z).toInt() and MASK_Z) shl (Int.SIZE_BITS - BITS_Z)) shr (Int.SIZE_BITS - BITS_Z)

    inline fun plusX(): SectionPosition {
        assertPosition(this.x < MAX_X)
        return SectionPosition(raw + X * 1)
    }

    inline fun plusX(x: Int): SectionPosition {
        assertPosition(this.x + x, -MAX_X, MAX_X)
        return SectionPosition(raw + X * x)
    }

    inline fun minusX(): SectionPosition {
        assert(this.x > -MAX_X)
        return SectionPosition(raw - X * 1)
    }

    inline fun plusY(): SectionPosition {
        assertPosition(this.y < MAX_Y)
        return SectionPosition(raw + Y * 1)
    }

    inline fun plusY(y: Int): SectionPosition {
        assertPosition(this.y + y, MIN_Y, MAX_Y)
        return SectionPosition(raw + Y * y)
    }

    inline fun minusY(): SectionPosition {
        assert(this.y > MIN_Y)
        return SectionPosition(raw - Y * 1)
    }

    inline fun plusZ(): SectionPosition {
        assert(this.z < MAX_Z)
        return SectionPosition(raw + Z * 1)
    }

    inline fun plusZ(z: Int): SectionPosition {
        assertPosition(this.z + z, -MAX_Z, MAX_Z)
        return SectionPosition(raw + Z * z)
    }

    inline fun minusZ(): SectionPosition {
        assert(this.z > -MAX_Z)
        return SectionPosition(raw - Z * 1)
    }

    inline fun with(x: Int = this.x, y: Int = this.y, z: Int = this.z) = SectionPosition(x, y, z)

    inline operator fun plus(value: Int) = SectionPosition(this.x + value, this.y + value, this.z + value)
    inline operator fun minus(value: Int) = SectionPosition(this.x - value, this.y - value, this.z - value)
    inline operator fun times(value: Int) = SectionPosition(this.x * value, this.y * value, this.z * value)
    inline operator fun div(value: Int) = SectionPosition(this.x / value, this.y * value, this.z / value)

    inline operator fun plus(position: SectionPosition) = SectionPosition(this.x + position.x, this.y + position.y, this.z + position.z)
    inline operator fun minus(position: SectionPosition) = SectionPosition(this.x - position.x, this.y - position.y, this.z - position.z)

    inline operator fun plus(position: ChunkPosition) = SectionPosition(this.x + position.x, this.y, this.z + position.z)
    inline operator fun minus(position: ChunkPosition) = SectionPosition(this.x - position.x, this.y, this.z - position.z)

    inline operator fun plus(direction: Directions) = SectionPosition(this.x + direction.vector.x, this.y + direction.vector.y, this.z + direction.vector.z)
    inline operator fun minus(direction: Directions) = SectionPosition(this.x - direction.vector.x, this.y - direction.vector.y, this.z - direction.vector.z)

    inline operator fun unaryMinus() = SectionPosition(-this.x, -this.y, -this.z)
    inline operator fun unaryPlus() = this


    inline operator fun component1() = x
    inline operator fun component2() = y
    inline operator fun component3() = z

    inline fun length2() = (x * x + y * y + z * z)

    inline fun sectionIndex(minSection: SectionHeight): SectionIndex = this.y - minSection
    inline val chunkPosition get() = ChunkPosition(x, z)

    override fun toText() = "(${this.x.format()} ${this.y.format()} ${this.z.format()})"
    override fun toString() = "c($x $y $z)"


    companion object {
        const val BITS_X = 22
        const val MASK_X = (1 shl BITS_X) - 1
        const val SHIFT_X = 0

        const val BITS_Z = 22
        const val MASK_Z = (1 shl BITS_Z) - 1
        const val SHIFT_Z = BITS_X

        const val BITS_Y = 8
        const val MASK_Y = (1 shl BITS_Y) - 1
        const val SHIFT_Y = BITS_X + BITS_Z

        const val X = 1L shl SHIFT_X
        const val Z = 1L shl SHIFT_Z
        const val Y = 1L shl SHIFT_Y


        const val MAX_X = BlockPosition.MAX_X shr 4
        const val MIN_Y = BlockPosition.MIN_Y shr 4
        const val MAX_Y = BlockPosition.MAX_Y shr 4
        const val MAX_Z = BlockPosition.MAX_Z shr 4


        fun of(chunkPosition: ChunkPosition, sectionHeight: Int) = SectionPosition(chunkPosition.x, sectionHeight, chunkPosition.z)
        fun of(chunkPosition: ChunkPosition, sectionIndex: SectionIndex, minSection: SectionHeight) = SectionPosition(chunkPosition.x, sectionIndex + minSection, chunkPosition.z)


        val EMPTY = SectionPosition(0, 0, 0)
    }
}
