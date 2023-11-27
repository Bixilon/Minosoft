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

package de.bixilon.minosoft.data.registries.registries.registry

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.identified.AliasedIdentified
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.integrated.IntegratedRegistry
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.codec.IdentifierCodec
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

open class Registry<T : RegistryItem>(
    override var parent: AbstractRegistry<T>? = null,
    val codec: IdentifierCodec<T>? = null,
    val integrated: IntegratedRegistry<T>? = null,
    val metaType: MetaTypes = MetaTypes.NONE,
    var flattened: Boolean = false,
) : AbstractRegistry<T> {
    protected val idValueMap: Int2ObjectOpenHashMap<T> = Int2ObjectOpenHashMap()
    protected val valueIdMap: Object2IntOpenHashMap<T> = Object2IntOpenHashMap()
    protected val resourceLocationMap: MutableMap<ResourceLocation, T> = HashMap()

    override val size: Int
        get() {
            val value = valueIdMap.size
            parent?.let {
                return value + it.size
            }
            return value
        }

    override operator fun get(any: Any?): T? {
        return when (any) {
            null -> null
            is Number -> getOrNull(any.toInt())
            is ResourceLocation -> get(any)
            is String -> get(any)
            is Identified -> get(any.identifier)
            else -> TODO()
        }
    }

    open operator fun get(resourceLocation: ResourceLocation): T? {
        return resourceLocationMap[resourceLocation] ?: parent?.get(resourceLocation)
    }

    open operator fun set(any: Any, value: T) {
        when (any) {
            is Int -> {
                idValueMap[any] = value
                valueIdMap[value] = any
            }

            is ResourceLocation -> resourceLocationMap[any] = value
            is Identified -> resourceLocationMap[any.identifier] = value
            is AliasedIdentified -> {
                for (resourceLocation in any.identifiers) {
                    resourceLocationMap[resourceLocation] = value
                }
            }

            else -> TODO("Can not set $any, value=$value")
        }
    }

    open operator fun get(resourceLocation: String): T? {
        return get(resourceLocation.toResourceLocation())
    }

    open operator fun get(resourceLocation: Identified): T? {
        return get(resourceLocation.identifier)
    }

    override fun getOrNull(id: Int): T? {
        return idValueMap[id] ?: parent?.getOrNull(id)
    }

    override fun getId(value: T): Int {
        return valueIdMap[value] ?: parent?.getId(value)!!
    }

    override fun update(data: Map<String, Any>?, version: Version, registries: Registries?) {
        if (data == null) return
        for ((name, value) in data) {
            check(value is Map<*, *>)
            val id = value["id"]?.toInt()?.let { if (metaType != MetaTypes.NONE && !flattened) metaType.modify(it, value["meta"]?.toInt() ?: 0) else it }
            addItem(name.toResourceLocation(), id, value.unsafeCast(), version, registries)
        }
    }

    override fun update(data: List<JsonObject>, version: Version, registries: Registries?) {
        for (entry in data) {
            val name = (entry["name"] ?: entry["key"])?.toResourceLocation() ?: throw IllegalArgumentException("Can not find name: $entry")
            val id = entry["id"]?.toInt()

            val entryData = entry["element"]?.toJsonObject() ?: entry
            addItem(name, id, entryData, version, registries)
        }
    }

    protected open fun deserialize(identifier: ResourceLocation, data: JsonObject, version: Version, registries: Registries?): T? {
        if (registries != null) {
            integrated?.build(identifier, registries, data)?.let { return it }
        }

        if (codec == null) {
            throw IllegalStateException("codec is null!")
        }
        return codec.deserialize(registries, identifier, data)
    }

    override fun addItem(identifier: ResourceLocation, id: Int?, data: JsonObject, version: Version, registries: Registries?): T? {
        val item = deserialize(identifier, data, version, registries) ?: return null

        if (id != null) {
            idValueMap[id] = item
            valueIdMap[item] = id
        }
        resourceLocationMap[identifier] = item

        return item
    }

    open fun postInit(registries: Registries) {
        for ((_, value) in resourceLocationMap) {
            value.inject(registries)
            value.postInit(registries)
        }
    }

    override fun toString(): String {
        return super.toString() + ": ${resourceLocationMap.size}x"
    }

    override fun clear() {
        resourceLocationMap.clear()
        idValueMap.clear()
        valueIdMap.clear()
    }

    override fun noParentIterator(): Iterator<T> {
        return resourceLocationMap.values.iterator()
    }

    override fun optimize() {
        idValueMap.trim()
        valueIdMap.trim()
    }
}
