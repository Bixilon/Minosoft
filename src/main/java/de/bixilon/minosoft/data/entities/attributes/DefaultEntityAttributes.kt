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

package de.bixilon.minosoft.data.entities.attributes

import de.bixilon.minosoft.util.KUtil.asResourceLocation

object DefaultEntityAttributes {
    // ToDo

    val GENERIC_MAX_HEALTH = "minecraft:generic.max_health".asResourceLocation()
    val GENERIC_FOLLOW_RANGE = "minecraft:generic.follow_range".asResourceLocation()
    val GENERIC_KNOCKBACK_RESISTANCE = "minecraft:generic.knockback_resistance".asResourceLocation()
    val GENERIC_MOVEMENT_SPEED = "minecraft:generic.movement_speed".asResourceLocation()
    val GENERIC_ATTACK_KNOCKBACK = "minecraft:generic.attack_knockback".asResourceLocation()
    val GENERIC_ARMOR = "minecraft:generic.armor".asResourceLocation()
    val GENERIC_ARMOR_TOUGHNESS = "minecraft:generic.armor_toughness".asResourceLocation()
}
