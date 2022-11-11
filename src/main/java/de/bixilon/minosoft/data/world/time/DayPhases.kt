/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.time

enum class DayPhases(private val progressCalculator: (Int) -> Float) {
    SUNRISE({ (it - 23000) / 1000.0f }),
    DAY({ it / 12000.0f }),
    SUNSET({ (it - 12000) / 1000.0f }), // I love this song: https://www.youtube.com/watch?v=URma_gu1aNE
    NIGHT({ (it - 13000) / 10000.9f }),
    ;

    fun getProgress(time: Int): Float {
        return progressCalculator(time)
    }

    companion object {
        fun of(time: Int): DayPhases {
            if (time > 23000) {
                return SUNRISE
            }
            if (time < 12000) {
                return DAY
            }
            if (time in 12000 until 13000) {
                return SUNSET
            }
            return NIGHT
        }
    }
}
