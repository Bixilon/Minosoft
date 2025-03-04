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
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.format

@JvmInline
value class ChunkPosition(
    inline val raw: Long,
) : TextFormattable {

    constructor() : this(0, 0)

    constructor(x: Int, z: Int) : this((z.toLong() shl SHIFT_Z) or (x.toLong() shl SHIFT_X)) {
        assertPosition(x, -MAX_X, MAX_X)
        assertPosition(z, -MAX_Z, MAX_Z)
    }

    inline val x: Int get() = (raw ushr SHIFT_X).toInt() and MASK_X
    inline val z: Int get() = (raw ushr SHIFT_Z).toInt() and MASK_Z


    inline fun plusX(): ChunkPosition {
        assertPosition(this.x < MAX_X)
        return ChunkPosition(raw + X * 1)
    }

    inline fun plusX(x: Int): ChunkPosition {
        assertPosition(this.x + x, -MAX_X, MAX_X)
        return ChunkPosition(raw + X * x)
    }

    inline fun minusX(): ChunkPosition {
        assert(this.x > -MAX_X)
        return ChunkPosition(raw - X * 1)
    }

    inline fun plusZ(): ChunkPosition {
        assert(this.z < MAX_Z)
        return ChunkPosition(raw + Z * 1)
    }

    inline fun plusZ(z: Int): ChunkPosition {
        assertPosition(this.z + z, -MAX_Z, MAX_Z)
        return ChunkPosition(raw + Z * z)
    }

    inline fun minusZ(): ChunkPosition {
        assert(this.z > -MAX_Z)
        return ChunkPosition(raw - Z * 1)
    }

    inline fun with(x: Int = this.x, z: Int = this.z) = ChunkPosition(x, z)

    inline operator fun plus(value: Int) = ChunkPosition(this.x + value, this.z + value)
    inline operator fun minus(value: Int) = ChunkPosition(this.x - value, this.z - value)
    inline operator fun times(value: Int) = ChunkPosition(this.x * value, this.z * value)
    inline operator fun div(value: Int) = ChunkPosition(this.x / value, this.z / value)

    inline operator fun plus(position: ChunkPosition) = ChunkPosition(this.x + position.x, this.z + position.z)
    inline operator fun minus(position: ChunkPosition) = ChunkPosition(this.x - position.x, this.z - position.z)

    inline operator fun plus(direction: Directions) = ChunkPosition(this.x + direction.vector.x, this.z + direction.vector.z)
    inline operator fun minus(direction: Directions) = ChunkPosition(this.x - direction.vector.x, this.z - direction.vector.z)

    inline operator fun unaryMinus() = ChunkPosition(-this.x, -this.z)
    inline operator fun unaryPlus() = this

    inline fun blockPosition(x: Int, y: Int, z: Int) = BlockPosition(this.x * ProtocolDefinition.SECTION_WIDTH_X + x, y, this.z * ProtocolDefinition.SECTION_WIDTH_Z + z)
    inline fun blockPosition(position: InChunkPosition) = BlockPosition(this.x * ProtocolDefinition.SECTION_WIDTH_X + position.x, position.y, this.z * ProtocolDefinition.SECTION_WIDTH_Z + position.z)

    inline infix fun and(mask: Int) = ChunkPosition(x and mask, z and mask)

    override fun toText() = "(${this.x.format()} ${this.z.format()})"
    override fun toString() = "c($x $z)"


    companion object {
        const val BITS_X = 32
        const val MASK_X = (1 shl BITS_X) - 1
        const val SHIFT_X = 0

        const val BITS_Z = 32
        const val MASK_Z = (1 shl BITS_Z) - 1
        const val SHIFT_Z = BITS_X

        const val X = 1L shl SHIFT_X
        const val Z = 1L shl SHIFT_Z


        const val MAX_X = Int.MAX_VALUE // TODO
        const val MAX_Z = Int.MAX_VALUE // TODO


        val EMPTY = ChunkPosition(0, 0)
    }
}
