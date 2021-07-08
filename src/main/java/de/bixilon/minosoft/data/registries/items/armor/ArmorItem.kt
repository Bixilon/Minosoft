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

package de.bixilon.minosoft.data.registries.items.armor

import de.bixilon.minosoft.data.inventory.InventorySlots
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.util.KUtil.unsafeCast

open class ArmorItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : Item(resourceLocation, registries, data) {
    val protection = data["defense"].unsafeCast<Float>()
    val toughness = data["toughness"].unsafeCast<Float>()
    val equipmentSlot = data["equipment_slot"].unsafeCast<String>().let { InventorySlots.EquipmentSlots[it] }
    val knockbackResistance = data["knockback_resistance"]?.unsafeCast<Float>() ?: 0.0f
}
