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

package de.bixilon.minosoft.data.mappings.registry

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.util.collections.Clearable
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap

open class Registry<T : RegistryItem>(
    private var parentRegistry: Registry<T>? = null,
) : Iterable<T>, Clearable, Parentable<Registry<T>> {
    private var initialized = false
    private val idValueMap: MutableMap<Int, T> = mutableMapOf()
    private val valueIdMap: MutableMap<T, Int> = mutableMapOf()
    private val resourceLocationMap: MutableMap<ResourceLocation, T> = mutableMapOf()


    open fun get(resourceLocation: ResourceLocation): T? {
        return resourceLocationMap[resourceLocation] ?: parentRegistry?.get(resourceLocation)
    }

    open fun get(id: Int): T {
        return idValueMap[id] ?: parentRegistry?.get(id)!!
    }

    open fun getId(value: T): Int {
        return valueIdMap[value] ?: parentRegistry?.getId(value)!!
    }


    override fun setParent(parent: Registry<T>?) {
        this.parentRegistry = parent
    }

    open fun initialize(data: Map<ResourceLocation, JsonObject>?, mappings: VersionMapping?, deserializer: ResourceLocationDeserializer<T>, flattened: Boolean = true, metaType: MetaTypes = MetaTypes.NONE): Registry<T> {
        check(!initialized) { "Already initialized" }

        if (data == null) {
            return this
        }

        for ((resourceLocation, value) in data) {
            val item = deserializer.deserialize(mappings, resourceLocation, value) ?: continue
            value["id"]?.asInt?.let { id ->
                var itemId = id
                if (!flattened) {
                    when (metaType) {
                        MetaTypes.NONE -> {
                        }
                        MetaTypes.BITS_4 -> {
                            itemId = itemId shl 4
                        }
                        MetaTypes.BITS_16 -> {
                            itemId = itemId shl 16
                        }
                    }
                    value["meta"]?.asInt?.let { meta ->
                        itemId = itemId or meta
                    }
                }
                idValueMap[id] = item
                valueIdMap[item] = id
            }
            resourceLocationMap[resourceLocation] = item
        }
        initialized = true
        return this
    }

    open fun initialize(data: JsonObject?, mappings: VersionMapping?, deserializer: ResourceLocationDeserializer<T>, flattened: Boolean = true, metaType: MetaTypes = MetaTypes.NONE): Registry<T> {
        return initialize(data?.toResourceLocationMap(), mappings, deserializer, flattened, metaType)
    }


    fun postInit(versionMapping: VersionMapping) {
        for ((_, value) in resourceLocationMap) {
            value.postInit(versionMapping)
        }
    }

    fun setData(resourceLocationMap: Map<ResourceLocation, T> = mapOf(), idValueMap: Map<Int, T> = mapOf(), valueIdMap: Map<T, Int> = mapOf()) {
        this.resourceLocationMap.clear()
        this.resourceLocationMap.putAll(resourceLocationMap)
        this.idValueMap.clear()
        this.idValueMap.putAll(idValueMap)
        this.valueIdMap.clear()
        this.valueIdMap.putAll(valueIdMap)
    }

    override fun toString(): String {
        return super.toString() + ": ${resourceLocationMap.size}x"
    }

    override fun clear() {
        resourceLocationMap.clear()
        idValueMap.clear()
        valueIdMap.clear()
    }

    enum class MetaTypes {
        NONE,
        BITS_4,
        BITS_16,
    }

    override fun iterator(): Iterator<T> {
        return resourceLocationMap.values.iterator()
    }
}
