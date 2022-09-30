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

package de.bixilon.minosoft.data.registries.item.items.armor

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.minosoft.data.container.InventorySlots
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.item.ItemFactory
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries

open class ArmorItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : Item(resourceLocation, registries, data) {
    val protection = data["defense"].toFloat()
    val toughness = data["toughness"].toFloat()
    val equipmentSlot = data["equipment_slot"].unsafeCast<String>().let { InventorySlots.EquipmentSlots[it] }
    val knockbackResistance = data["knockback_resistance"]?.toFloat() ?: 0.0f

    companion object : ItemFactory<ArmorItem> {

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): ArmorItem {
            return ArmorItem(resourceLocation, registries, data)
        }
    }
}
