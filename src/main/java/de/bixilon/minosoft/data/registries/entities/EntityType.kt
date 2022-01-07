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

package de.bixilon.minosoft.data.registries.entities

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.data.DefaultEntityFactories
import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.items.SpawnEggItem
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.registries.registries.registry.Translatable
import de.bixilon.minosoft.datafixer.EntityAttributeFixer.fix
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import glm_.vec3.Vec3d
import java.util.*

data class EntityType(
    override val resourceLocation: ResourceLocation,
    override val translationKey: ResourceLocation?,
    val width: Float,
    val height: Float,
    val sizeFixed: Boolean,
    val fireImmune: Boolean,
    val attributes: Map<ResourceLocation, Double>,
    val factory: EntityFactory<out Entity>,
    val spawnEgg: SpawnEggItem?,
) : RegistryItem(), Translatable {


    override fun toString(): String {
        return resourceLocation.toString()
    }

    fun build(connection: PlayConnection, position: Vec3d, rotation: EntityRotation, entityMetaData: EntityMetaData?, versionId: Int): Entity? {
        return DefaultEntityFactories.buildEntity(factory, connection, position, rotation, entityMetaData, versionId)
    }

    companion object : ResourceLocationDeserializer<EntityType> {
        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): EntityType? {
            check(registries != null) { "Registries is null!" }

            data["meta"]?.toJsonObject()?.let {
                for ((minosoftFieldName, index) in it) {
                    val minosoftField = EntityMetaDataFields[minosoftFieldName.lowercase(Locale.getDefault())]
                    registries.entityMetaIndexMap[minosoftField] = index.unsafeCast()
                }
            }
            if (data["width"] == null) {
                // abstract entity
                return null
            }

            val attributes: MutableMap<ResourceLocation, Double> = mutableMapOf()

            data["attributes"]?.toJsonObject()?.let {
                for ((attributeResourceLocation, value) in it) {
                    attributes[ResourceLocation.getResourceLocation(attributeResourceLocation).fix()] = value.unsafeCast()
                }
            }

            return EntityType(
                resourceLocation = resourceLocation,
                translationKey = data["translation_key"]?.toResourceLocation(),
                width = data["width"].unsafeCast(),
                height = data["height"].unsafeCast(),
                fireImmune = data["fire_immune"]?.toBoolean() ?: false,
                sizeFixed = data["size_fixed"]?.toBoolean() ?: false,
                attributes = attributes.toMap(),
                factory = DefaultEntityFactories[resourceLocation] ?: error("Can not find entity factory for $resourceLocation"),
                spawnEgg = registries.itemRegistry[data["spawn_egg_item"]]?.nullCast(), // ToDo: Not yet in PixLyzer
            )
        }
    }
}
