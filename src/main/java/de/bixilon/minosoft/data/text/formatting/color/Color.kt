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

package de.bixilon.minosoft.data.text.formatting.color

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kutil.ansi.ANSI

interface Color {
    val ansi: String get() = ANSI.rgb(red, green, blue)

    val red: Int
    val green: Int
    val blue: Int

    val redf: Float
    val greenf: Float
    val bluef: Float

    val rgb: Int

    fun rgb() = RGBColor(red, green, blue)
    fun rgba() = RGBAColor(red, green, blue)

    companion object {
        const val BITS = 8

        const val MIN = 0
        const val VALUES = (1 shl BITS)
        const val MAX = VALUES - 1

        const val MASK = VALUES - 1

        const val TIMES = VALUES * VALUES


        fun Int.clamp() = this.clamp(MIN, MAX)

        fun toFloat(value: Int) = value * (1.0f / MAX)
        fun fromFloat(value: Float) = (value * MAX).toInt()
    }
}
