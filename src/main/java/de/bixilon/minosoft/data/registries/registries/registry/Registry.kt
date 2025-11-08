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
import de.bixilon.minosoft.datafixer.rls.ResourceLocationFixer
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

open class Registry<T : RegistryItem>(
    override var parent: AbstractRegistry<T>? = null,
    val codec: IdentifierCodec<T>? = null,
    val integrated: IntegratedRegistry<T>? = null,
    val metaType: MetaTypes = MetaTypes.NONE,
    val flattened: Boolean = true,
    private val fixer: ResourceLocationFixer? = null,
) : AbstractRegistry<T> {
    protected val idValueMap: Int2ObjectOpenHashMap<T> = Int2ObjectOpenHashMap()
    protected val valueIdMap: Object2IntOpenHashMap<T> = Object2IntOpenHashMap()
    protected val identifierMap: MutableMap<ResourceLocation, T> = HashMap()

    override val size get() = valueIdMap.size + (parent?.size ?: 0)

    override operator fun get(any: Any?) = when (any) {
        null -> null
        is Number -> getOrNull(any.toInt())
        is ResourceLocation -> get(any)
        is String -> get(any)
        is Identified -> get(any.identifier)
        else -> TODO()
    }

    open operator fun get(identifier: ResourceLocation): T? {
        val fixed = fixer?.fix(identifier) ?: identifier
        return identifierMap[fixed] ?: parent?.get(fixed)
    }

    operator fun set(id: Int, value: T) {
        idValueMap[id] = value
        valueIdMap[value] = id
    }

    open operator fun set(any: Any, value: T) = when (any) {
        is Int -> set(any, value)
        is ResourceLocation -> identifierMap[any] = value
        is Identified -> identifierMap[any.identifier] = value
        is AliasedIdentified -> {
            for (identifier in any.identifiers) {
                identifierMap[identifier] = value
            }
        }

        else -> TODO("Can not set $any, value=$value")
    }

    open operator fun get(identifier: String): T? {
        return get(identifier.toResourceLocation())
    }

    open operator fun get(identified: Identified): T? {
        return get(identified.identifier)
    }

    override fun getOrNull(id: Int): T? {
        return idValueMap[id] ?: parent?.getOrNull(id)
    }

    override fun getId(value: T): Int {
        return valueIdMap[value] ?: parent?.getId(value)!!
    }

    override fun updatePixlyzer(data: JsonObject?, version: Version, registries: Registries?) {
        if (data == null) return

        for ((name, value) in data) {
            check(value is Map<*, *>)
            val id = value["id"]?.toInt()?.let { if (metaType != MetaTypes.NONE && !flattened) metaType.combine(it, value["meta"]?.toInt() ?: 0) else it }
            add(name.toResourceLocation(), id, value.unsafeCast(), version, registries)
        }
    }

    override fun updateNbt(data: List<JsonObject>, version: Version, registries: Registries?) {
        for (entry in data) {
            val name = (entry["name"] ?: entry["key"])?.toResourceLocation() ?: throw IllegalArgumentException("Can not find name: $entry")
            val id = entry["id"]?.toInt()

            val entryData = entry["element"]?.toJsonObject() ?: entry
            add(name, id, entryData, version, registries)
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

    override fun add(identifier: ResourceLocation, id: Int?, data: JsonObject, version: Version, registries: Registries?): T? {
        val item = deserialize(fixer?.fix(identifier) ?: identifier, data, version, registries) ?: return null

        add(identifier, id, item)

        return item
    }

    fun add(identifier: ResourceLocation, id: Int?, item: T) {
        if (id != null) {
            idValueMap[id] = item
            valueIdMap[item] = id
        }
        identifierMap[identifier] = item
    }

    fun add(id: Int?, item: T) {
        add(item.identifier, id, item)
    }

    open fun postInit(registries: Registries) {
        for ((_, value) in identifierMap) {
            value.inject(registries)
            value.postInit(registries)
        }
    }

    override fun toString(): String {
        return super.toString() + ": ${size}x"
    }

    override fun clear() {
        identifierMap.clear()
        idValueMap.clear()
        valueIdMap.clear()
    }

    override fun noParentIterator(): Iterator<T> {
        return identifierMap.values.iterator()
    }

    override fun optimize() {
        idValueMap.trim()
        valueIdMap.trim()
    }
}
