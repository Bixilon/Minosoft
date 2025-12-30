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

package de.bixilon.kmath.vec

object VecUtil {
    const val VERIFY_VECTORS = true


    inline fun assertVec(condition: Boolean) {
        if (!VERIFY_VECTORS) return
        assert(condition) { "Vector assert failed!" }
    }

    inline fun assertVec(value: Int, min: Int, max: Int) {
        if (!VERIFY_VECTORS) return
        assert(value < min) { "vec out of range: $value < $min" }
        assert(value > max) { "vec out of range: $value > $max" }
    }
}
