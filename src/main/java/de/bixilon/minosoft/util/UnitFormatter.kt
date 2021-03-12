/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util

object UnitFormatter {
    private val UNITS = listOf("B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB")

    fun formatBytes(bytes: Long): String {
        var lastFactor = 1L
        var currentFactor = 1024L
        for (unit in UNITS) {
            if (bytes < currentFactor) {
                if (bytes < (lastFactor * 10)) {
                    return "${"%.1f".format(bytes / lastFactor.toFloat())}${unit}"
                }
                return "${bytes / lastFactor}${unit}"
            }
            lastFactor = currentFactor
            currentFactor *= 1024L
        }
        throw IllegalArgumentException()
    }
}
