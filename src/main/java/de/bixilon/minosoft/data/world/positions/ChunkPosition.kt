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

import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.format

@JvmInline
value class ChunkPosition(
    inline val raw: Long,
) : TextFormattable {
    init {
        TODO()
    }

    constructor() : this(0, 0)

    constructor(x: Int, z: Int) : this((z shl SHIFT_Z) or (x shl SHIFT_X)) {
        assert(x >= 0)
        assert(x <= ProtocolDefinition.SECTION_MAX_X)
        assert(z >= 0)
        assert(z <= ProtocolDefinition.SECTION_MAX_Z)
    }

    inline val x: Int get() = (raw and MASK_X) shr SHIFT_X
    inline val z: Int get() = (raw and MASK_Z) shr SHIFT_Z
    inline val xz: Int get() = (raw and MASK_Z or MASK_X)


    inline fun plusX(): ChunkPosition {
        assert(this.x < ProtocolDefinition.SECTION_MAX_X)
        return ChunkPosition(raw + X * 1)
    }

    inline fun plusX(x: Int): ChunkPosition {
        assert(this.x + x < ProtocolDefinition.SECTION_MAX_X)
        assert(this.x + x > 0)
        return ChunkPosition(raw + X * x)
    }

    inline fun minusX(): ChunkPosition {
        assert(this.x > 0)
        return ChunkPosition(raw - X * 1)
    }

    inline fun plusZ(): ChunkPosition {
        assert(this.z < ProtocolDefinition.SECTION_MAX_Z)
        return ChunkPosition(raw + Z * 1)
    }

    inline fun plusZ(z: Int): ChunkPosition {
        assert(this.z + z < ProtocolDefinition.SECTION_MAX_Z)
        assert(this.z + z > 0)
        return ChunkPosition(raw + Z * z)
    }

    inline fun minusZ(): ChunkPosition {
        assert(this.z > 0)
        return ChunkPosition(raw - Z * 1)
    }

    inline fun with(x: Int = this.x, z: Int = this.z) = ChunkPosition(x, z)

    inline operator fun plus(position: ChunkPosition) = ChunkPosition(this.x + position.x, this.z + position.z)
    inline operator fun minus(position: ChunkPosition) = ChunkPosition(this.x - position.x, this.z - position.z)

    inline operator fun unaryMinus() = ChunkPosition(-this.x, -this.z)
    inline operator fun unaryPlus() = this

    inline fun blockPosition(x: Int, y: Int, z: Int) = BlockPosition(this.x * ProtocolDefinition.SECTION_WIDTH_X + x, y, this.z * ProtocolDefinition.SECTION_WIDTH_Z + z)
    inline fun blockPosition(position: InChunkPosition) = BlockPosition(this.x * ProtocolDefinition.SECTION_WIDTH_X + position.x, position.y, this.z * ProtocolDefinition.SECTION_WIDTH_Z + position.z)

    override fun toText() = "(${this.x.format()} ${this.z.format()})"


    val index: Int
        get() = when {
            x == -1 && z == -1 -> 0
            x == -1 && z == 0 -> 1
            x == -1 && z == 1 -> 2
            x == 0 && z == -1 -> 3
            x == 0 && z == 1 -> 4
            x == 1 && z == -1 -> 5
            x == 1 && z == 0 -> 6
            x == 1 && z == 1 -> 7
            else -> Broken("Can not get neighbour chunk from offset $this")
        }

    companion object {
        const val MASK_X = 0x00F
        const val SHIFT_X = 0

        const val MASK_Z = 0x0F0
        const val SHIFT_Z = 4

        const val X = 1 shl SHIFT_X
        const val Z = 1 shl SHIFT_Z


        val EMPTY = ChunkPosition(0, 0)
    }
}
