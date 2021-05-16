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

package de.bixilon.minosoft.data.mappings.items.armor

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.inventory.InventorySlots
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.items.Item
import de.bixilon.minosoft.data.mappings.versions.VersionMapping

open class ArmorItem(
    resourceLocation: ResourceLocation,
    versionMapping: VersionMapping,
    data: JsonObject,
) : Item(resourceLocation, versionMapping, data) {
    val protection = data["defense"].asFloat
    val toughness = data["toughness"].asFloat
    val equipmentSlot = data["equipment_slot"].asString.let { InventorySlots.EquipmentSlots.NAME_MAP[it]!! }
    val knockbackResistance = data["knockback_resistance"]?.asFloat ?: 0.0f
}
