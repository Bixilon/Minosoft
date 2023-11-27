/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.registries.effects.attributes.AttributeType
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.items.pixlyzer.SpawnEggItem
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.codec.IdentifierCodec
import de.bixilon.minosoft.datafixer.rls.EntityAttributeFixer.fixEntityAttribute
import de.bixilon.minosoft.gui.rendering.entities.factory.RegisteredEntityModelFactory
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.get
import java.lang.reflect.Modifier
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.jvm.javaField

data class EntityType(
    override val identifier: ResourceLocation,
    override val translationKey: ResourceLocation?,
    val width: Float,
    val height: Float,
    val attributes: Map<AttributeType, Double>,
    val factory: EntityFactory<out Entity>,
    val spawnEgg: SpawnEggItem?,
    var modelFactory: RegisteredEntityModelFactory<*>? = null,
) : RegistryItem(), Translatable {

    override fun toString(): String {
        return identifier.toString()
    }

    fun build(connection: PlayConnection, position: Vec3d, rotation: EntityRotation, entityData: EntityData?, uuid: UUID?, versionId: Int): Entity? {
        return DefaultEntityFactories.buildEntity(factory, connection, position, rotation, entityData, uuid, versionId)
    }

    companion object : IdentifierCodec<EntityType> {
        override fun deserialize(registries: Registries?, identifier: ResourceLocation, data: Map<String, Any>): EntityType? {
            check(registries != null) { "Registries is null!" }
            val factory = DefaultEntityFactories[identifier]

            data["meta", "data"]?.toJsonObject()?.let {
                val fields: MutableMap<String, EntityDataField> = mutableMapOf()
                val dataClass = DefaultEntityFactories.ABSTRACT_ENTITY_DATA_CLASSES[identifier]?.companionObject ?: if (factory != null) factory::class else null
                if (dataClass == null) {
                    Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Can not find entity data class for $identifier, fields $it" }
                    return@let
                }
                for (member in dataClass.members) {
                    if (member !is KProperty1<*, *>) {
                        continue
                    }
                    val field = member.javaField ?: continue
                    if (!Modifier.isStatic(field.modifiers)) {
                        continue
                    }
                    field.isAccessible = true
                    if (field.type != EntityDataField::class.java) {
                        continue
                    }
                    val dataField = field.get(null) as EntityDataField
                    for (name in dataField.names) {
                        fields[name] = dataField
                    }
                }
                for ((fieldName, index) in it) {
                    val fieldType = fields[fieldName]
                    if (fieldType == null) {
                        Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Can not find entity data $fieldName for $identifier" }
                        continue
                    }
                    registries.entityDataIndexMap[fieldType] = index.toInt()
                }
            }
            if (data["width"] == null) {
                // abstract entity
                return null
            }

            if (factory == null) {
                throw NullPointerException("Can not find entity factory for $identifier")
            }

            val attributes: MutableMap<AttributeType, Double> = mutableMapOf()

            data["attributes"]?.toJsonObject()?.let {
                for ((name, value) in it) {
                    val type = MinecraftAttributes[name.toResourceLocation().fixEntityAttribute()]
                    if (type == null) {
                        Log.log(LogMessageType.LOADING, LogLevels.VERBOSE) { "Can not get entity attribute type: $name" }
                        continue
                    }
                    attributes[type] = value.unsafeCast()
                }
            }

            return EntityType(
                identifier = identifier,
                translationKey = data["translation_key"]?.toResourceLocation(),
                width = data["width"].unsafeCast(),
                height = data["height"].unsafeCast(),
                attributes = attributes,
                factory = factory,
                spawnEgg = registries.item[data["spawn_egg_item"]]?.nullCast(), // ToDo: Not yet in PixLyzer
            )
        }
    }
}
