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

package de.bixilon.kmath.mat.mat3.f

class UnsafeMat3f(
    val array: FloatArray
) {

    init {
        assert(array.size == Mat3f.LENGTH)
    }

    fun final() = Mat3f(this)
    fun mutable() = MMat3f(this)

    inline operator fun get(row: Int, column: Int): Float {
        // assert(x in 0 until 3)
        // assert(y in 0 until 3)
        return array[column * 3 + row]
    }

    inline operator fun set(row: Int, column: Int, value: Float) {
        // assert(x in 0 until 3)
        // assert(y in 0 until 3)
        array[column * 3 + row] = value
    }


    override fun toString(): String {
        return "${this[0, 0]} ${this[0, 1]} ${this[0, 2]}}\n" +
            "${this[1, 0]} ${this[1, 1]} ${this[1, 2]}\n" +
            "${this[2, 0]} ${this[2, 1]} ${this[2, 2]}\n"
    }


    override fun hashCode() = array.contentHashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is UnsafeMat3f) return false
        return other.array.contentEquals(array)
    }
}
