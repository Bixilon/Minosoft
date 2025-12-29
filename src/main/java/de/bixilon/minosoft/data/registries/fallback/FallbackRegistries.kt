/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.fallback

import de.bixilon.minosoft.assets.IntegratedAssets
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJsonObject
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.entities.EntityAnimations
import de.bixilon.minosoft.data.entities.EntityObjectType
import de.bixilon.minosoft.data.entities.block.BlockDataDataType
import de.bixilon.minosoft.data.entities.data.types.EntityDataTypes
import de.bixilon.minosoft.data.registries.PluginChannel
import de.bixilon.minosoft.data.registries.chat.ChatMessageType
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.registries.entities.variants.CatVariant
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.registries.registry.Registry
import de.bixilon.minosoft.data.registries.registries.registry.ResourceLocationRegistry
import de.bixilon.minosoft.data.registries.registries.registry.version.PerVersionEnumRegistry
import de.bixilon.minosoft.data.registries.registries.registry.version.PerVersionRegistry
import de.bixilon.minosoft.protocol.packets.c2s.play.entity.EntityActionC2SP
import de.bixilon.minosoft.protocol.packets.s2c.play.title.TitleS2CF
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

object FallbackRegistries {
    private val registries: MutableMap<PerVersionRegistry<*, *>, ResourceLocation> = mutableMapOf()
    private val enums: MutableMap<PerVersionEnumRegistry<*>, ResourceLocation> = mutableMapOf()

    val EQUIPMENT_SLOTS = PerVersionEnumRegistry(EquipmentSlots).register(minecraft("entity/equipment"))
    val ENTITY_OBJECT = PerVersionRegistry { Registry(codec = EntityObjectType) }.register(minecraft("entity/objects"))
    val ENTITY_DATA_TYPES = PerVersionEnumRegistry(EntityDataTypes).register(minecraft("entity/data_types"))
    val ENTITY_ACTIONS = PerVersionEnumRegistry(EntityActionC2SP.EntityActions).register(minecraft("entity/actions"))
    val ENTITY_ANIMATION = PerVersionEnumRegistry(EntityAnimations).register(minecraft("entity/animations"))

    val CAT_VARIANT: PerVersionRegistry<CatVariant, Registry<CatVariant>> = PerVersionRegistry { Registry(codec = CatVariant) }.register(minecraft("entity/variant/cat"))

    val TITLE_ACTIONS = PerVersionEnumRegistry(TitleS2CF.TitleActions).register(minecraft("title_actions"))
    val DEFAULT_PLUGIN_CHANNELS: PerVersionRegistry<PluginChannel, Registry<PluginChannel>> = PerVersionRegistry { Registry(codec = PluginChannel) }.register(minecraft("channels"))
    val MESSAGE_TYPES: PerVersionRegistry<ChatMessageType, Registry<ChatMessageType>> = PerVersionRegistry { Registry(codec = ChatMessageType) }.register(minecraft("message_types"))
    val VIBRATION_SOURCE: PerVersionRegistry<ResourceLocation, ResourceLocationRegistry> = PerVersionRegistry { ResourceLocationRegistry() }.register(minecraft("vibration_source"))


    val BLOCK_DATA_TYPE: PerVersionRegistry<BlockDataDataType, Registry<BlockDataDataType>> = PerVersionRegistry { Registry(codec = BlockDataDataType) }.register(minecraft("block_data_types"))

    val CONTAINER_TYPE: PerVersionRegistry<ContainerType, Registry<ContainerType>> = PerVersionRegistry { Registry(codec = ContainerType) }.register(minecraft("container_type"))

    val GAME_EVENT: PerVersionRegistry<ResourceLocation, ResourceLocationRegistry> = PerVersionRegistry { ResourceLocationRegistry() }.register(minecraft("game_events"))
    val WORLD_EVENT: PerVersionRegistry<ResourceLocation, ResourceLocationRegistry> = PerVersionRegistry { ResourceLocationRegistry() }.register(minecraft("world_events"))


    fun load() {
        Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Loading default registries..." }
        if (this.registries.isEmpty() || this.enums.isEmpty()) throw IllegalArgumentException("No fallback registries?")

        for ((registry, file) in registries) {
            val data = IntegratedAssets.DEFAULT[file.prefix("mappings/registries/").suffix(".json")].readJsonObject()
            registry.initialize(data)
        }
        for ((registry, file) in enums) {
            val data = IntegratedAssets.DEFAULT[file.prefix("mappings/enums/").suffix(".json")].readJsonObject()
            registry.initialize(data)
        }
        this.registries.clear()
        this.enums.clear()

        Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Loaded default registries!" }
    }

    private fun <T : PerVersionRegistry<*, *>> T.register(file: ResourceLocation): T {
        registries[this] = file
        return this
    }

    private fun <T : PerVersionEnumRegistry<*>> T.register(file: ResourceLocation): T {
        enums[this] = file
        return this
    }
}
