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

package de.bixilon.minosoft.data.registries.enchantment.armor

import de.bixilon.minosoft.data.container.equipment.ArmorSlots
import de.bixilon.minosoft.data.container.equipment.EntityEquipment
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft

interface MovementEnchantment : ArmorEnchantment {

    object DepthStrider : Enchantment(), MovementEnchantment, Identified {
        override val identifier = minecraft("depth_strider")
        override val slots: Set<ArmorSlots> = ArmorSlots.ALL
    }

    object SoulSpeed : Enchantment(), MovementEnchantment, Identified {
        override val identifier = minecraft("soul_speed")
        override val slots: Set<ArmorSlots> = setOf(ArmorSlots.FEET)
    }

    object SwiftSneak : Enchantment(), MovementEnchantment, Identified {
        override val identifier = minecraft("swift_sneak")
        override val slots: Set<ArmorSlots> = setOf(ArmorSlots.LEGS)

        fun EntityEquipment.getSwiftSneakBoost(): Float {
            val amplifier = this[SwiftSneak]
            if (amplifier <= 0) return 0.0f
            return amplifier * 0.15f
        }
    }
}
