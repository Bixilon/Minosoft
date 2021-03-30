/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.data.inventory.InventorySlots
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketTitle
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap

object DefaultRegistries {
    private val ENUM_RESOURCE_LOCATION = ResourceLocation("minosoft:mapping/enums.json")
    private var initialized = false

    val EQUIPMENT_SLOTS_REGISTRY = PerVersionRegistry(InventorySlots.EquipmentSlots)
    val HAND_EQUIPMENT_SLOTS_REGISTRY = PerVersionRegistry(InventorySlots.EquipmentSlots)
    val ARMOR_EQUIPMENT_SLOTS_REGISTRY = PerVersionRegistry(InventorySlots.EquipmentSlots)
    val ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY = PerVersionRegistry(InventorySlots.EquipmentSlots)

    val ENTITY_META_DATA_DATA_TYPES_REGISTRY = PerVersionRegistry(EntityMetaData.EntityMetaDataDataTypes)

    val TITLE_ACTIONS_REGISTRY = PerVersionRegistry(PacketTitle.TitleActions)


    fun load() {
        check(!initialized) { "Already initialized!" }

        val enumJson = Minosoft.MINOSOFT_ASSETS_MANAGER.readJsonAsset(ENUM_RESOURCE_LOCATION).asJsonObject.toResourceLocationMap()

        EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation("equipment_slots")]!!)
        HAND_EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation("hand_equipment_slots")]!!)
        ARMOR_EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation("armor_equipment_slots")]!!)
        ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation("armor_stand_equipment_slots")]!!)

        ENTITY_META_DATA_DATA_TYPES_REGISTRY.initialize(enumJson[ResourceLocation("entity_meta_data_data_types")]!!)

        TITLE_ACTIONS_REGISTRY.initialize(enumJson[ResourceLocation("title_actions")]!!)

        initialized = true
    }

}
