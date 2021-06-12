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

package de.bixilon.minosoft.data.registries.effects

import de.bixilon.minosoft.util.KUtil.asResourceLocation

object DefaultStatusEffects {
    val BLINDNESS = "minecraft:blindness".asResourceLocation()
    val SLOW_FALLING = "minecraft:slow_falling".asResourceLocation()
    val LEVITATION = "minecraft:levitation".asResourceLocation()
    val JUMP_BOOST = "minecraft:jump_boost".asResourceLocation()
    val HASTE = "minecraft:haste".asResourceLocation()
    val MINING_FATIGUE = "minecraft:mining_fatigue".asResourceLocation()
}
