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

package de.bixilon.minosoft.util

import java.util.*

object Backports {

    fun Random.nextIntPort(min: Int, max: Int): Int {
        return nextInt((max - min) + 1) + min
    }

    fun Random.nextLongPort(min: Long, max: Long): Long {
        return nextLong() % (max - min) + min
    }

    fun Random.nextDoublePort(min: Double = Double.MIN_VALUE, max: Double = Double.MAX_VALUE): Double {
        return min + this.nextDouble() * (max - min)
    }

    fun Random.nextFloatPort(min: Float = Float.MIN_VALUE, max: Float = Float.MAX_VALUE): Float {
        return min + this.nextFloat() * (max - min)
    }
}
