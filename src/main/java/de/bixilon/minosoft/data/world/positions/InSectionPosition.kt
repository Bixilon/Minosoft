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
value class InSectionPosition(
    inline val index: Int,
) : TextFormattable {

    constructor(x: Int, y: Int, z: Int) : this((y shl SHIFT_Y) or (z shl SHIFT_Z) or (x shl SHIFT_X)) {
        assert(x >= 0)
        assert(x <= ProtocolDefinition.SECTION_MAX_X)
        assert(y >= 0)
        assert(y <= ProtocolDefinition.SECTION_MAX_Y)
        assert(z >= 0)
        assert(z <= ProtocolDefinition.SECTION_MAX_Z)
    }

    inline val x: Int get() = (index and MASK_X) shr SHIFT_X
    inline val y: Int get() = (index and MASK_Y) shr SHIFT_Y
    inline val z: Int get() = (index and MASK_Z) shr SHIFT_Z


    inline fun plusX(): InSectionPosition {
        assert(this.x < ProtocolDefinition.SECTION_MAX_X)
        return InSectionPosition(index + X * 1)
    }

    inline fun plusX(x: Int): InSectionPosition {
        assert(this.x + x < ProtocolDefinition.SECTION_MAX_X)
        assert(this.x + x > 0)
        return InSectionPosition(index + X * x)
    }

    inline fun minusX(): InSectionPosition {
        assert(this.x > 0)
        return InSectionPosition(index - X * 1)
    }

    inline fun plusY(): InSectionPosition {
        assert(this.y < ProtocolDefinition.SECTION_MAX_Y)
        return InSectionPosition(index + Y * 1)
    }

    inline fun plusY(y: Int): InSectionPosition {
        assert(this.y + y < ProtocolDefinition.SECTION_MAX_Y)
        assert(this.y + y > 0)
        return InSectionPosition(index + Y * y)
    }

    inline fun minusY(): InSectionPosition {
        assert(this.y > 0)
        return InSectionPosition(index - Y * 1)
    }

    inline fun plusZ(): InSectionPosition {
        assert(this.z < ProtocolDefinition.SECTION_MAX_Z)
        return InSectionPosition(index + Z * 1)
    }

    inline fun plusZ(z: Int): InSectionPosition {
        assert(this.z + z < ProtocolDefinition.SECTION_MAX_Z)
        assert(this.z + z > 0)
        return InSectionPosition(index + Z * z)
    }

    inline fun minusZ(): InSectionPosition {
        assert(this.z > 0)
        return InSectionPosition(index - Z * 1)
    }

    inline fun with(x: Int = this.x, y: Int = this.y, z: Int = this.z) = InSectionPosition(x, y, z)

    inline operator fun plus(position: InSectionPosition) = InSectionPosition(this.x + position.x, this.y + position.y, this.z + position.z)

    companion object {
        const val MASK_X = 0x00F
        const val SHIFT_X = 0

        const val MASK_Z = 0x0F0
        const val SHIFT_Z = 4

        const val MASK_Y = 0xF00
        const val SHIFT_Y = 8

        const val X = 1 shl SHIFT_X
        const val Z = 1 shl SHIFT_Z
        const val Y = 1 shl SHIFT_Y


        val EMPTY = InSectionPosition(0, 0, 0)
    }

    override fun toText() = "(${this.x.format()} ${this.y.format()} ${this.z.format()})"
}
