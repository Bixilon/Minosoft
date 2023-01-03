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

import de.bixilon.minosoft.data.container.ArmorSlots
import de.bixilon.minosoft.data.container.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.enchantment.slots.SlotSpecificEnchantment
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.item.items.armor.ArmorItem
import de.bixilon.minosoft.util.KUtil.minecraft

interface ArmorEnchantment : SlotSpecificEnchantment {
    val slots: Set<ArmorSlots>


    override fun canApply(entity: Entity, slot: EquipmentSlots, item: ItemStack): Boolean {
        val item = item.item.item
        if (item !is ArmorItem) {
            return false
        }
        val armorSlot = when (slot) {
            EquipmentSlots.FEET -> ArmorSlots.FEET
            EquipmentSlots.LEGS -> ArmorSlots.LEGS
            EquipmentSlots.CHEST -> ArmorSlots.CHEST
            EquipmentSlots.HEAD -> ArmorSlots.HEAD
            else -> return false
        }
        if (armorSlot !in item.armorSlot) {
            return false
        }
        return armorSlot in this.slots
    }


    object AquaAffinity : Enchantment(), ArmorEnchantment, Identified {
        override val identifier = minecraft("aqua_affinity")
        override val slots: Set<ArmorSlots> = ArmorSlots.ALL
    }

    object DepthStrider : Enchantment(), ArmorEnchantment, Identified {
        override val identifier = minecraft("depth_strider")
        override val slots: Set<ArmorSlots> = ArmorSlots.ALL
    }

    object SoulSpeed : Enchantment(), ArmorEnchantment, Identified {
        override val identifier = minecraft("soul_speed")
        override val slots: Set<ArmorSlots> = setOf(ArmorSlots.FEET)
    }
}
