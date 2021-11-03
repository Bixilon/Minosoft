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

package de.bixilon.minosoft.data.player

data class Abilities(
    var isInvulnerable: Boolean = false,
    var isFlying: Boolean = false,
    var canFly: Boolean = false,
    var creative: Boolean = false, // ToDo: This is the check if we are in creative mode, but no exactly sure...

    var flyingSpeed: Double = 0.05,
    var walkingSpeed: Double = 0.1,
)
