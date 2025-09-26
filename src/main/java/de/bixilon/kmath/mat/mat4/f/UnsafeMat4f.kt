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

package de.bixilon.kmath.mat.mat4.f

class UnsafeMat4f(
    val array: FloatArray
) {

    init {
        assert(array.size == Mat4f.LENGTH)
    }

    fun final() = Mat4f(this)
    fun mutable() = MMat4f(this)

    inline operator fun get(x: Int, y: Int): Float {
        // assert(x in 0 until 4)
        // assert(y in 0 until 4)
        return array[(y shl 2) or x]
    }

    inline operator fun set(x: Int, y: Int, value: Float) {
        // assert(x in 0 until 4)
        // assert(y in 0 until 4)
        array[(y shl 2) or x] = value
    }


    override fun hashCode() = array.contentHashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is UnsafeMat4f) return false
        return other.array.contentEquals(array)
    }
}
