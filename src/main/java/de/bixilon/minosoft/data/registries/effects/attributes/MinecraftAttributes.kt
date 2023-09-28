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

package de.bixilon.minosoft.data.registries.effects.attributes

import de.bixilon.minosoft.data.registries.MinecraftDefaults
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft

object MinecraftAttributes : MinecraftDefaults<AttributeType>() {
    val MAX_HEALTH = AttributeType(minecraft("generic.max_health"), 20.0).register()
    val FOLLOW_RANGE = AttributeType(minecraft("generic.follow_range"), 32.0, 0.0, 2048.0).register()
    val KNOCKBACK_RESISTANCE = AttributeType(minecraft("generic.knockback_resistance"), 0.0, 0.0, 1.0).register()
    val MOVEMENT_SPEED = AttributeType(minecraft("generic.movement_speed"), 0.7).register()
    val FLYING_SPEED = AttributeType(minecraft("generic.flying_speed"), 0.4).register()
    val ATTACK_DAMAGE = AttributeType(minecraft("generic.attack_damage"), 2.0, 0.0, 2048.0).register()
    val ATTACK_SPEED = AttributeType(minecraft("generic.attack_speed"), 1.0, 0.0, 2048.0).register() // TODO: values
    val ATTACK_KNOCKBACK = AttributeType(minecraft("generic.attack_knockback"), 0.0, 0.0, 5.0).register()
    val ARMOR = AttributeType(minecraft("generic.armor"), 0.0, 0.0, 30.0).register()
    val ARMOR_TOUGHNESS = AttributeType(minecraft("generic.armor_toughness"), 0.0, 0.0, 20.0).register()
    val LUCK = AttributeType(minecraft("generic.luck"), 0.0, -1024.0, 1024.0).register()
    val MAX_ABSORPTION = AttributeType(minecraft("generic.max_absorption"), 0.0, 0.0, 2048.0).register()
    val ZOMBIE_SPAWN_REINFORCEMENTS = AttributeType(minecraft("zombie.spawn_reinforcements"), 0.0, 0.0, 1.0).register()
    val HORSE_JUMP_STRENGTH = AttributeType(minecraft("horse.jump_strength"), 0.7, 0.0, 2.0).register()
}
