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

package de.bixilon.minosoft.data.physics.enchantments

import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.registries.enchantment.armor.MovementEnchantment
import de.bixilon.minosoft.data.registries.item.items.armor.materials.IronArmor
import de.bixilon.minosoft.test.IT

object EnchantmentTestUtil {

    fun LocalPlayerEntity.applySoulSpeed(level: Int) {
        val boots = ItemStack(IT.REGISTRIES.item[IronArmor.IronBoots]!!)
        boots.enchanting.enchantments[IT.REGISTRIES.enchantment[MovementEnchantment.SoulSpeed]!!] = level

        this.equipment[EquipmentSlots.FEET] = boots
    }
}
