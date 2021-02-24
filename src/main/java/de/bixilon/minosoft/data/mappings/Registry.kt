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

import com.google.common.collect.HashBiMap
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap

open class Registry<T : RegistryItem>(
    private var parentRegistry: Registry<T>? = null,
    initialSize: Int = 50,
) : Iterable<T> {
    private var initialized = false
    private val idMap = HashBiMap.create<Int, T>(initialSize)
    private val resourceLocationMap = HashBiMap.create<ResourceLocation, T>(initialSize)


    open fun get(resourceLocation: ResourceLocation): T? {
        return resourceLocationMap[resourceLocation] ?: parentRegistry?.get(resourceLocation)
    }

    open fun get(id: Int): T? {
        return idMap[id] ?: parentRegistry?.get(id)
    }

    open fun getId(value: T): Int {
        return idMap.inverse()[value] ?: parentRegistry?.getId(value)!!
    }

    fun setParent(registry: Registry<T>?) {
        this.parentRegistry = registry
    }

    open fun initialize(data: Map<ResourceLocation, JsonObject>?, mappings: VersionMapping, deserializer: ResourceLocationDeserializer<T>, flattened: Boolean = true, metaType: MetaTypes = MetaTypes.NONE) {
        check(!initialized) { "Already initialized" }

        if (data == null) {
            return
        }

        for ((resourceLocation, value) in data) {
            val item = deserializer.deserialize(mappings, resourceLocation, value)
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
                idMap[id] = item
            }
            resourceLocationMap[resourceLocation] = item
        }
        initialized = true
    }

    open fun initialize(data: JsonObject?, mappings: VersionMapping, deserializer: ResourceLocationDeserializer<T>, flattened: Boolean = true, metaType: MetaTypes = MetaTypes.NONE) {
        initialize(ResourceLocationJsonMap.create(data), mappings, deserializer, flattened, metaType)
    }


    fun postInit(versionMapping: VersionMapping) {
        for ((_, value) in resourceLocationMap) {
            value.postInit(versionMapping)
        }
    }

    fun setData(resourceLocationMap: HashBiMap<ResourceLocation, T> = HashBiMap.create(), idMap: HashBiMap<Int, T> = HashBiMap.create()) {
        this.resourceLocationMap.clear()
        this.resourceLocationMap.putAll(resourceLocationMap)
        this.idMap.clear()
        this.idMap.putAll(idMap)
    }

    override fun toString(): String {
        return super.toString() + ": ${resourceLocationMap.size}x"
    }

    fun unload() {
        resourceLocationMap.clear()
        idMap.clear()
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
