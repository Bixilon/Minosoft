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

package de.bixilon.minosoft.data.registries.effects.attributes

import de.bixilon.minosoft.util.KUtil.toResourceLocation

object DefaultStatusEffectAttributeNames {
    val GENERIC_MAX_HEALTH = "minecraft:generic.max_health".toResourceLocation()
    val GENERIC_FOLLOW_RANGE = "minecraft:generic.follow_range".toResourceLocation()
    val GENERIC_KNOCKBACK_RESISTANCE = "minecraft:generic.knockback_resistance".toResourceLocation()
    val GENERIC_MOVEMENT_SPEED = "minecraft:generic.movement_speed".toResourceLocation()
    val GENERIC_ATTACK_KNOCKBACK = "minecraft:generic.attack_knockback".toResourceLocation()
    val GENERIC_ARMOR = "minecraft:generic.armor".toResourceLocation()
    val GENERIC_ARMOR_TOUGHNESS = "minecraft:generic.armor_toughness".toResourceLocation()
}
