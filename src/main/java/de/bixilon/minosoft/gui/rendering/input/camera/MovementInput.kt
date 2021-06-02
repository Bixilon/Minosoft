/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.input.camera

data class MovementInput(
    val pressingForward: Boolean = false,
    val pressingBack: Boolean = false,
    val pressingLeft: Boolean = false,
    val pressingRight: Boolean = false,
    val jumping: Boolean = false,
    val sneaking: Boolean = false,
    val sprinting: Boolean = false,
    val flyDown: Boolean = false,
    val flyUp: Boolean = false,
    val toggleFlyDown: Boolean = false,
) {
    val movementForward = getMovementSpeed(pressingForward, pressingBack)
    var movementSideways = getMovementSpeed(pressingLeft, pressingRight)
    var flyYMovement = getMovementSpeed(flyUp, flyDown)

    companion object {
        private fun getMovementSpeed(key1: Boolean, key2: Boolean): Float {
            if (key1 && key2) {
                return 0.0f
            }
            if (key1) {
                return 1.0f
            }
            if (key2) {
                return -1.0f
            }
            return 0.0f
        }
    }
}
