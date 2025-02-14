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

import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.format

@JvmInline
value class ChunkPosition(
    inline val index: Long,
) : TextFormattable {

    constructor(x: Int, z: Int) : this((z shl SHIFT_Z) or (x shl SHIFT_X)) {
        assert(x >= 0)
        assert(x <= ProtocolDefinition.SECTION_MAX_X)
        assert(z >= 0)
        assert(z <= ProtocolDefinition.SECTION_MAX_Z)
    }

    inline val x: Int get() = (index and MASK_X) shr SHIFT_X
    inline val z: Int get() = (index and MASK_Z) shr SHIFT_Z
    inline val xz: Int get() = (index and MASK_Z or MASK_X)


    inline fun plusX(): ChunkPosition {
        assert(this.x < ProtocolDefinition.SECTION_MAX_X)
        return ChunkPosition(index + X * 1)
    }

    inline fun plusX(x: Int): ChunkPosition {
        assert(this.x + x < ProtocolDefinition.SECTION_MAX_X)
        assert(this.x + x > 0)
        return ChunkPosition(index + X * x)
    }

    inline fun minusX(): ChunkPosition {
        assert(this.x > 0)
        return ChunkPosition(index - X * 1)
    }

    inline fun plusZ(): ChunkPosition {
        assert(this.z < ProtocolDefinition.SECTION_MAX_Z)
        return ChunkPosition(index + Z * 1)
    }

    inline fun plusZ(z: Int): ChunkPosition {
        assert(this.z + z < ProtocolDefinition.SECTION_MAX_Z)
        assert(this.z + z > 0)
        return ChunkPosition(index + Z * z)
    }

    inline fun minusZ(): ChunkPosition {
        assert(this.z > 0)
        return ChunkPosition(index - Z * 1)
    }

    inline fun with(x: Int = this.x, y: Int = this.y, z: Int = this.z) = ChunkPosition(x, y, z)

    inline operator fun plus(position: ChunkPosition) = ChunkPosition(this.x + position.x, this.z + position.z)

    inline operator fun unaryMinus() = ChunkPosition(-this.x, -this.z)
    inline operator fun unaryPlus() = this

    override fun toText() = "(${this.x.format()} ${this.z.format()})"

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
