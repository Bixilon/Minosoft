/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.util

object BitByte {
    fun isBitSet(`in`: Long, pos: Int): Boolean {
        val mask = 1L shl pos
        return `in` and mask == mask
    }

    fun isBitSet(`in`: Int, pos: Int): Boolean {
        val mask = 1 shl pos
        return `in` and mask == mask
    }

    @JvmStatic
    fun isBitMask(`in`: Int, mask: Int): Boolean {
        return `in` and mask == mask
    }

    fun getBitCount(input: Long): Byte {
        var ret: Byte = 0
        for (i in 0 until java.lang.Long.SIZE) {
            if (isBitSet(input, i)) {
                ret++
            }
        }
        return ret
    }

    fun isBitSetShort(`in`: Short, pos: Int): Boolean {
        val mask = 1 shl pos
        return `in`.toInt() and mask == mask
    }

    infix fun Int.isBit(bit: Int): Boolean {
        return isBitSet(this, bit)
    }

    @JvmName("isBitMask1")
    infix fun Int.isBitMask(bitMask: Int): Boolean {
        return isBitMask(this, bitMask)
    }
}
