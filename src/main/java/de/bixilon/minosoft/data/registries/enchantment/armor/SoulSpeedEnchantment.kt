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

package de.bixilon.minosoft.data.registries.enchantment.armor

import de.bixilon.minosoft.data.container.ArmorSlots
import de.bixilon.minosoft.data.registries.CompanionResourceLocation
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.util.KUtil.minecraft

object SoulSpeedEnchantment : Enchantment(), ArmorEnchantment, CompanionResourceLocation {
    override val RESOURCE_LOCATION = minecraft("soul_speed")
    override val slots: Set<ArmorSlots> = setOf(ArmorSlots.FEET)
}
