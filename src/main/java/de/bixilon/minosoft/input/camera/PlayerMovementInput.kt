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

package de.bixilon.minosoft.input.camera

data class PlayerMovementInput(
    val forward: Boolean = false,
    val backward: Boolean = false,
    val left: Boolean = false,
    val right: Boolean = false,

    val jump: Boolean = false,
    val sneak: Boolean = false,
    val sprint: Boolean = false,

    val flyDown: Boolean = false,
    val flyUp: Boolean = false,
) {
    val forwards = getMovementSpeed(forward, backward)
    val sideways = getMovementSpeed(left, right)
    val upwards = getMovementSpeed(flyUp, flyDown)

    private fun getMovementSpeed(positive: Boolean, negative: Boolean): Float {
        var value = 0
        if (positive) {
            value++
        }
        if (negative) {
            value--
        }
        return value.toFloat()
    }


    fun isEmpty(): Boolean {
        return !(forward || backward || left || right)
    }
}
