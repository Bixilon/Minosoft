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

package de.bixilon.minosoft.datafixer.rsl

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.datafixer.DataFixerUtil.asResourceLocationMap

object EntityAttributeFixer : ResourceLocationFixer {
    private val RENAMES: Map<ResourceLocation, ResourceLocation> = mapOf(
        "generic.maxHealth" to "generic.max_health",
        "zombie.spawnReinforcements" to "zombie.spawn_reinforcements",

        "horse.jumpStrength" to "horse.jump_strength",

        "generic.followRange" to "generic.follow_range",

        "generic.knockbackResistance" to "generic.knockback_resistance",

        "generic.movementSpeed" to "generic.movement_speed",

        "generic.flyingSpeed" to "generic.flying_speed",

        "generic.attackDamage" to "generic.attack_damage",
        "generic.attackKnockback" to "generic.attack_knockback",
        "generic.attackSpeed" to "generic.attack_speed",
        "generic.armorToughness" to "generic.armor_toughness",
    ).asResourceLocationMap()


    override fun _fix(resourceLocation: ResourceLocation): ResourceLocation {
        return RENAMES.getOrDefault(resourceLocation, resourceLocation)
    }
}
