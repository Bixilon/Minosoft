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
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.format

@JvmInline
value class InChunkPosition(
    inline val raw: Int,
) : TextFormattable {

    constructor() : this(0, 0, 0)

    constructor(x: Int, y: Int, z: Int) : this(((y and 0xFFF) shl SHIFT_Y) or (z shl SHIFT_Z) or (x shl SHIFT_X)) {
        assertPosition(x, 0, ProtocolDefinition.SECTION_MAX_X)
        assertPosition(y, ProtocolDefinition.CHUNK_MIN_Y, ProtocolDefinition.CHUNK_MAX_Y)
        assertPosition(z, 0, ProtocolDefinition.SECTION_MAX_Z)
    }

    inline val x: Int get() = (raw ushr SHIFT_X) and MASK_X
    inline val y: Int get() = (((raw ushr SHIFT_Y) and MASK_Y) shl (Int.SIZE_BITS - BITS_Y)) shr (Int.SIZE_BITS - BITS_Y)
    inline val z: Int get() = (raw ushr SHIFT_Z) and MASK_Z
    inline val xz: Int get() = raw and ((MASK_X shl SHIFT_X) or (MASK_Z shl SHIFT_Z))


    inline fun plusX(): InChunkPosition {
        assertPosition(this.x < ProtocolDefinition.SECTION_MAX_X)
        return InChunkPosition(raw + X * 1)
    }

    inline fun plusX(x: Int): InChunkPosition {
        assertPosition(this.x + x, 0, ProtocolDefinition.SECTION_MAX_X)
        return InChunkPosition(raw + X * x)
    }

    inline fun minusX(): InChunkPosition {
        assertPosition(this.x > 0)
        return InChunkPosition(raw - X * 1)
    }

    inline fun plusY(): InChunkPosition {
        assertPosition(this.y < ProtocolDefinition.CHUNK_MAX_Y)
        return InChunkPosition(raw + Y * 1)
    }

    inline fun plusY(y: Int): InChunkPosition {
        assertPosition(this.y + y, ProtocolDefinition.CHUNK_MIN_Y, ProtocolDefinition.CHUNK_MAX_Y)
        return InChunkPosition(raw + Y * y)
    }

    inline fun minusY(): InChunkPosition {
        assertPosition(this.y > ProtocolDefinition.CHUNK_MIN_Y)
        return InChunkPosition(raw - Y * 1)
    }

    inline fun plusZ(): InChunkPosition {
        assertPosition(this.z < ProtocolDefinition.SECTION_MAX_Z)
        return InChunkPosition(raw + Z * 1)
    }

    inline fun plusZ(z: Int): InChunkPosition {
        assertPosition(this.z + z, 0, ProtocolDefinition.SECTION_MAX_Z)
        return InChunkPosition(raw + Z * z)
    }

    inline fun minusZ(): InChunkPosition {
        assertPosition(this.z > 0)
        return InChunkPosition(raw - Z * 1)
    }

    inline fun with(x: Int = this.x, y: Int = this.y, z: Int = this.z) = InChunkPosition(x, y, z)

    inline operator fun plus(position: InChunkPosition) = InChunkPosition(this.x + position.x, this.y + position.y, this.z + position.z)

    inline operator fun plus(direction: Directions) = InChunkPosition(this.x + direction.vector.x, this.y + direction.vector.y, this.z + direction.vector.z)
    inline operator fun minus(direction: Directions) = InChunkPosition(this.x - direction.vector.x, this.y - direction.vector.y, this.z - direction.vector.z)

    override fun toText() = "(${this.x.format()} ${this.y.format()} ${this.z.format()})"
    override fun toString() = "c($x $y $z)"

    inline val inSectionPosition get() = InSectionPosition(x, y.inSectionHeight, z)
    inline val sectionHeight get() = y.sectionHeight

    companion object {
        const val BITS_X = 4
        const val MASK_X = (1 shl BITS_X) - 1
        const val SHIFT_X = 0

        const val BITS_Z = 4
        const val MASK_Z = (1 shl BITS_Z) - 1
        const val SHIFT_Z = BITS_X

        const val BITS_Y = 12
        const val MASK_Y = (1 shl BITS_Y) - 1
        const val SHIFT_Y = BITS_X + BITS_Z

        const val X = 1 shl SHIFT_X
        const val Z = 1 shl SHIFT_Z
        const val Y = 1 shl SHIFT_Y


        val EMPTY = InChunkPosition(0, 0, 0)
    }
}
