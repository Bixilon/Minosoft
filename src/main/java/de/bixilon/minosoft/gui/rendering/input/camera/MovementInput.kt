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

package de.bixilon.minosoft.gui.rendering.input.camera

data class MovementInput(
    val forward: Boolean,
    val back: Boolean,
    val left: Boolean,
    val right: Boolean,
    val jumping: Boolean,
    val sneaking: Boolean,
    val sprinting: Boolean,
    val flyUp: Boolean,
    val flyDown: Boolean,
    val toggleFlyDown: Boolean,
) {
    val movementForward = getMovementSpeed(forward, back)
    var movementSideways = getMovementSpeed(left, right)
    var flyYMovement = getMovementSpeed(flyUp, flyDown)

    companion object {
        val EMPTY = MovementInput(forward = false, back = false, left = false, right = false, jumping = false, sneaking = false, sprinting = false, flyUp = false, flyDown = false, toggleFlyDown = false)

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
