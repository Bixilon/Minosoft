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
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.util.FileUtil.readJsonObject
import de.bixilon.minosoft.data.container.EquipmentSlots
import de.bixilon.minosoft.data.entities.EntityAnimations
import de.bixilon.minosoft.data.entities.EntityObjectType
import de.bixilon.minosoft.data.entities.block.BlockDataDataType
import de.bixilon.minosoft.data.entities.data.types.EntityDataTypes
import de.bixilon.minosoft.data.registries.chat.ChatMessageType
import de.bixilon.minosoft.data.registries.entities.variants.CatVariant
import de.bixilon.minosoft.data.registries.other.containers.ContainerType
import de.bixilon.minosoft.data.registries.registries.registry.PerVersionEnumRegistry
import de.bixilon.minosoft.data.registries.registries.registry.PerVersionRegistry
import de.bixilon.minosoft.data.registries.registries.registry.Registry
import de.bixilon.minosoft.data.registries.registries.registry.ResourceLocationRegistry
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.EntityActionC2SP
import de.bixilon.minosoft.protocol.packets.s2c.play.title.TitleS2CF
import de.bixilon.minosoft.util.KUtil.minecraft
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

object DefaultRegistries {
    private val ENUM_RESOURCE_LOCATION = ResourceLocation("minosoft:mapping/enums.json")
    private val REGISTRIES_RESOURCE_LOCATION = ResourceLocation("minosoft:mapping/default_registries.json")
    private var initialized = false

    val EQUIPMENT_SLOTS_REGISTRY = PerVersionEnumRegistry(EquipmentSlots)
    val HAND_EQUIPMENT_SLOTS_REGISTRY = PerVersionEnumRegistry(EquipmentSlots)
    val ARMOR_EQUIPMENT_SLOTS_REGISTRY = PerVersionEnumRegistry(EquipmentSlots)
    val ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY = PerVersionEnumRegistry(EquipmentSlots)

    val ENTITY_DATA_TYPES_REGISTRY = PerVersionEnumRegistry(EntityDataTypes)

    val TITLE_ACTIONS_REGISTRY = PerVersionEnumRegistry(TitleS2CF.TitleActions)

    val ENTITY_ANIMATION_REGISTRY = PerVersionEnumRegistry(EntityAnimations)
    val ENTITY_ACTIONS_REGISTRY = PerVersionEnumRegistry(EntityActionC2SP.EntityActions)

    val ENTITY_OBJECT_REGISTRY: Registry<EntityObjectType> = Registry(codec = EntityObjectType)

    val BLOCK_DATA_TYPE_REGISTRY: PerVersionRegistry<BlockDataDataType, Registry<BlockDataDataType>> = PerVersionRegistry { Registry(codec = BlockDataDataType) }

    val DEFAULT_PLUGIN_CHANNELS_REGISTRY: PerVersionRegistry<PluginChannel, Registry<PluginChannel>> = PerVersionRegistry { Registry(codec = PluginChannel) }

    val CONTAINER_TYPE_REGISTRY: PerVersionRegistry<ContainerType, Registry<ContainerType>> = PerVersionRegistry { Registry(codec = ContainerType) }

    val GAME_EVENT_REGISTRY: PerVersionRegistry<ResourceLocation, ResourceLocationRegistry> = PerVersionRegistry { ResourceLocationRegistry() }
    val WORLD_EVENT_REGISTRY: PerVersionRegistry<ResourceLocation, ResourceLocationRegistry> = PerVersionRegistry { ResourceLocationRegistry() }

    val CAT_VARIANT_REGISTRY: PerVersionRegistry<CatVariant, Registry<CatVariant>> = PerVersionRegistry { Registry(codec = CatVariant) }


    val MESSAGE_TYPES_REGISTRY: PerVersionRegistry<ChatMessageType, Registry<ChatMessageType>> = PerVersionRegistry { Registry(codec = ChatMessageType) }


    fun load(latch: CountUpAndDownLatch) {
        check(!initialized) { "Already initialized!" }
        Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Loading default registries..." }

        val enumJson = Minosoft.MINOSOFT_ASSETS_MANAGER[ENUM_RESOURCE_LOCATION].readJsonObject().toResourceLocationMap()

        EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation("equipment_slots")].asJsonObject())
        HAND_EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation("hand_equipment_slots")].asJsonObject())
        ARMOR_EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation("armor_equipment_slots")].asJsonObject())
        ARMOR_STAND_EQUIPMENT_SLOTS_REGISTRY.initialize(enumJson[ResourceLocation("armor_stand_equipment_slots")].asJsonObject())

        ENTITY_DATA_TYPES_REGISTRY.initialize(enumJson[ResourceLocation("entity_data_data_types")].asJsonObject()) // ToDo

        TITLE_ACTIONS_REGISTRY.initialize(enumJson[ResourceLocation("title_actions")].asJsonObject())

        ENTITY_ANIMATION_REGISTRY.initialize(enumJson[ResourceLocation("entity_animations")].asJsonObject())
        ENTITY_ACTIONS_REGISTRY.initialize(enumJson[ResourceLocation("entity_actions")].asJsonObject())


        val registriesJson = Minosoft.MINOSOFT_ASSETS_MANAGER[REGISTRIES_RESOURCE_LOCATION].readJsonObject().toResourceLocationMap()

        DEFAULT_PLUGIN_CHANNELS_REGISTRY.initialize(registriesJson[ResourceLocation("default_plugin_channels")].asJsonObject(), PluginChannel)

        ENTITY_OBJECT_REGISTRY.rawUpdate(registriesJson[ResourceLocation("entity_objects")].asJsonObject(), null)

        BLOCK_DATA_TYPE_REGISTRY.initialize(registriesJson[ResourceLocation("block_data_data_types")].asJsonObject(), BlockDataDataType)

        CONTAINER_TYPE_REGISTRY.initialize(registriesJson[ResourceLocation("container_types")].asJsonObject(), ContainerType)

        GAME_EVENT_REGISTRY.initialize(registriesJson[ResourceLocation("game_events")].asJsonObject(), null)
        WORLD_EVENT_REGISTRY.initialize(registriesJson[ResourceLocation("world_events")].asJsonObject(), null)


        CAT_VARIANT_REGISTRY.initialize(registriesJson[ResourceLocation("variants/cat")].asJsonObject(), CatVariant)

        MESSAGE_TYPES_REGISTRY.initialize(registriesJson[minecraft("message_types")].asJsonObject(), ChatMessageType)

        initialized = true
        Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Loaded default registries!" }
    }
}
