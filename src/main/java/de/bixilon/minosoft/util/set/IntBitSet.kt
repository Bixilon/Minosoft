/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.set

@Deprecated("kutil 1.25")
class IntBitSet(private var data: Int = 0) : AbstractBitSet {

    override operator fun get(index: Int): Boolean {
        checkIndex(index)
        val mask = 1 shl index
        return (data and mask) != 0
    }

    override operator fun set(index: Int, value: Boolean) {
        checkIndex(index)
        val mask = 1 shl index
        data = if (value) {
            data or mask
        } else {
            data and mask.inv()
        }
    }

    override fun clear() {
        this.data = 0
    }

    override fun capacity() = Int.SIZE_BITS

    override fun length(): Int {
        var data = data
        var bit = 0

        while (data != 0) {
            data = data ushr 1
            bit++
        }
        return bit
    }

    public override fun clone(): IntBitSet {
        return IntBitSet(data)
    }

    override fun hashCode(): Int {
        return data
    }

    override fun equals(other: Any?): Boolean {
        if (other !is IntBitSet) return false
        return data == other.data
    }


    private inline fun checkIndex(index: Int) {
        if (index < 0 || index >= Int.SIZE_BITS) throw IndexOutOfBoundsException("Index out of bounds $index")
    }
}
