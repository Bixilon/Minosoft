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

package de.bixilon.minosoft.data.mappings.entities

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.DefaultEntityFactories
import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.meta.EntityMetaData
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.registry.RegistryItem
import de.bixilon.minosoft.data.mappings.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.mappings.registry.Translatable
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3

data class EntityType(
    override val resourceLocation: ResourceLocation,
    override val translationKey: String?,
    val width: Float,
    val height: Float,
    val sizeFixed: Boolean,
    val fireImmune: Boolean,
    val maxHealth: Float,
    val factory: EntityFactory<out Entity>,
) : RegistryItem, Translatable {

    fun build(connection: PlayConnection, position: Vec3, rotation: EntityRotation, entityMetaData: EntityMetaData?, versionId: Int): Entity? {
        return DefaultEntityFactories.buildEntity(factory, connection, position, rotation, entityMetaData, versionId)
    }

    companion object : ResourceLocationDeserializer<EntityType> {
        override fun deserialize(mappings: VersionMapping?, resourceLocation: ResourceLocation, data: JsonObject): EntityType? {
            check(mappings != null) { "VersionMapping is null!" }

            data["meta"]?.asJsonObject?.let {
                for ((minosoftFieldName, index) in it.entrySet()) {
                    val minosoftField = EntityMetaDataFields.valueOf(minosoftFieldName)
                    mappings.entityMetaIndexMap[minosoftField] = index.asInt
                }
            }
            if (data["width"] == null) {
                // abstract entity
                return null
            }

            return EntityType(
                resourceLocation = resourceLocation,
                translationKey = data["translation_key"]?.asString,
                width = data["width"].asFloat,
                height = data["height"].asFloat,
                fireImmune = data["fire_immune"]?.asBoolean ?: false,
                sizeFixed = data["size_fixed"]?.asBoolean ?: false,
                maxHealth = data["minecraft:generic.max_health"]?.asFloat ?: Float.MAX_VALUE,
                factory = DefaultEntityFactories.getEntityFactory(resourceLocation) ?: error("Can not find entity factory for $resourceLocation"),
            )
        }
    }
}
