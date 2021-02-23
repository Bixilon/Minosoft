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
import de.bixilon.minosoft.util.json.IdentifierJsonMap

open class Registry<T : RegistryItem>(
    private var parentRegistry: Registry<T>? = null,
    initialSize: Int = 50,
) {
    private var initialized = false
    private val idMap = HashBiMap.create<Int, T>(initialSize)
    private val identifierMap = HashBiMap.create<ModIdentifier, T>(initialSize)


    open fun get(identifier: ModIdentifier): T? {
        return identifierMap[identifier] ?: parentRegistry?.get(identifier)
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

    open fun initialize(data: Map<ModIdentifier, JsonObject>?, mappings: VersionMapping, deserializer: IdentifierDeserializer<T>, flattened: Boolean = true, metaType: MetaTypes = MetaTypes.NONE) {
        check(!initialized) { "Already initialized" }

        if (data == null) {
            return
        }

        for ((identifier, value) in data) {
            val item = deserializer.deserialize(mappings, identifier, value)
            value["id"].asInt.let { id ->
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
                    value["meta"].asInt.let { meta ->
                        itemId = itemId or meta
                    }
                }
                idMap[id] = item
            }
            identifierMap[identifier] = item
        }
        initialized = true
    }

    open fun initialize(data: JsonObject?, mappings: VersionMapping, deserializer: IdentifierDeserializer<T>, flattened: Boolean = true, metaType: MetaTypes = MetaTypes.NONE) {
        initialize(IdentifierJsonMap.create(data), mappings, deserializer, flattened, metaType)
    }


    fun postInit(versionMapping: VersionMapping) {
        for ((_, value) in identifierMap) {
            value.postInit(versionMapping)
        }
    }

    fun setData(identifierMap: HashBiMap<ModIdentifier, T> = HashBiMap.create(), idMap: HashBiMap<Int, T> = HashBiMap.create()) {
        this.identifierMap.clear()
        this.identifierMap.putAll(identifierMap)
        this.idMap.clear()
        this.idMap.putAll(idMap)
    }

    override fun toString(): String {
        return super.toString() + ": ${identifierMap.size}x"
    }

    fun unload() {
        identifierMap.clear()
        idMap.clear()
    }

    enum class MetaTypes {
        NONE,
        BITS_4,
        BITS_16,
    }
}
