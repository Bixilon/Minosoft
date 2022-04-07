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

package de.bixilon.minosoft.data.registries

import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.util.FileUtil.readJsonObject
import de.bixilon.minosoft.data.container.InventorySlots
import de.bixilon.minosoft.data.entities.EntityObjectType
import de.bixilon.minosoft.data.entities.block.BlockEntityMetaType
import de.bixilon.minosoft.data.entities.meta.EntityData
import de.bixilon.minosoft.data.registries.other.containers.ContainerType
import de.bixilon.minosoft.data.registries.other.game.event.GameEvent
import de.bixilon.minosoft.data.registries.registries.registry.PerVersionEnumRegistry
import de.bixilon.minosoft.data.registries.registries.registry.PerVersionRegistry
import de.bixilon.minosoft.data.registries.registries.registry.Registry
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.EntityActionC2SP
import de.bixilon.minosoft.protocol.packets.s2c.play.entity.EntityAnimationS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.title.TitleS2CF
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap

object DefaultRegistries {
    private val ENUM_RESOURCE_LOCATION = ResourceLocation("minosoft:mapping/enums.json")
    private val REGISTRIES_RESOURCE_LOCATION = ResourceLocation("minosoft:mapping/default_registries.json")
    private var initialized = false

    val EQUIPMENT_SLOTS_REGISTRY = PerVersionEnumRegistry(InventorySlots.EquipmentSlots)
    val HAND_EQUIPMENT_SLOTS_REGISTRY = PerVersionEnumRegistry(InventorySlots.EquipmentSlots)
    val ARMOR_EQUIPMENT_SLOTS_REGISTRY = PerVersionEnumRegistry(InventorySlots.EquipmentSlots)
    val ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY = PerVersionEnumRegistry(InventorySlots.EquipmentSlots)

    val ENTITY_META_DATA_DATA_TYPES_REGISTRY = PerVersionEnumRegistry(EntityData.EntityDataDataTypes)

    val TITLE_ACTIONS_REGISTRY = PerVersionEnumRegistry(TitleS2CF.TitleActions)

    val ENTITY_ANIMATION_REGISTRY = PerVersionEnumRegistry(EntityAnimationS2CP.EntityAnimations)
    val ENTITY_ACTIONS_REGISTRY = PerVersionEnumRegistry(EntityActionC2SP.EntityActions)

    val ENTITY_OBJECT_REGISTRY: Registry<EntityObjectType> = Registry()

    val BLOCK_ENTITY_META_TYPE_REGISTRY: PerVersionRegistry<BlockEntityMetaType> = PerVersionRegistry()

    val DEFAULT_PLUGIN_CHANNELS_REGISTRY: PerVersionRegistry<PluginChannel> = PerVersionRegistry()

    val CONTAINER_TYPE_REGISTRY: PerVersionRegistry<ContainerType> = PerVersionRegistry()
    val GAME_EVENT_REGISTRY: PerVersionRegistry<GameEvent> = PerVersionRegistry()


    fun load() {
        check(!initialized) { "Already initialized!" }

        val enumJson = Minosoft.MINOSOFT_ASSETS_MANAGER[ENUM_RESOURCE_LOCATION].readJsonObject().toResourceLocationMap()

        EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation("equipment_slots")].asJsonObject())
        HAND_EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation("hand_equipment_slots")].asJsonObject())
        ARMOR_EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation("armor_equipment_slots")].asJsonObject())
        ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation("armor_stand_equipment_slots")].asJsonObject())

        ENTITY_META_DATA_DATA_TYPES_REGISTRY.initialize(enumJson[ResourceLocation("entity_meta_data_data_types")].asJsonObject()) // ToDo

        TITLE_ACTIONS_REGISTRY.initialize(enumJson[ResourceLocation("title_actions")].asJsonObject())

        ENTITY_ANIMATION_REGISTRY.initialize(enumJson[ResourceLocation("entity_animations")].asJsonObject())
        ENTITY_ACTIONS_REGISTRY.initialize(enumJson[ResourceLocation("entity_actions")].asJsonObject())


        val registriesJson = Minosoft.MINOSOFT_ASSETS_MANAGER[REGISTRIES_RESOURCE_LOCATION].readJsonObject().toResourceLocationMap()

        DEFAULT_PLUGIN_CHANNELS_REGISTRY.initialize(registriesJson[ResourceLocation("default_plugin_channels")].asJsonObject(), PluginChannel)

        ENTITY_OBJECT_REGISTRY.rawInitialize(registriesJson[ResourceLocation("entity_objects")].asJsonObject(), null, EntityObjectType)

        BLOCK_ENTITY_META_TYPE_REGISTRY.initialize(registriesJson[ResourceLocation("block_entity_meta_data_types")].asJsonObject(), BlockEntityMetaType)

        CONTAINER_TYPE_REGISTRY.initialize(registriesJson[ResourceLocation("container_types")].asJsonObject(), ContainerType)
        GAME_EVENT_REGISTRY.initialize(registriesJson[ResourceLocation("game_events")].asJsonObject(), GameEvent)

        initialized = true
    }

}
