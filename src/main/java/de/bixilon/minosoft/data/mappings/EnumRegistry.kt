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

class EnumRegistry<T : RegistryEnumable>(
    private var parentRegistry: EnumRegistry<T>? = null,
    initialSize: Int = 50,
) {
    private var initialized = false
    private val idMap = HashBiMap.create<Int, T>(initialSize)
    private val nameMap = HashBiMap.create<String, T>(initialSize)


    fun get(name: String): T? {
        return nameMap[name] ?: parentRegistry?.get(name)
    }

    fun get(id: Int): T? {
        return idMap[id] ?: parentRegistry?.get(id)
    }

    fun getId(value: T): Int {
        return idMap.inverse()[value] ?: parentRegistry?.getId(value)!!
    }

    fun setParent(registry: EnumRegistry<T>?) {
        this.parentRegistry = registry
    }

    fun initialize(data: JsonObject?, mappings: VersionMapping, deserializer: IdDeserializer<T>) {
        check(!initialized) { "Already initialized" }

        if (data == null) {
            return
        }

        for ((id, value) in data.entrySet()) {
            check(value is JsonObject)
            var itemId = id.toInt()

            val item = deserializer.deserialize(mappings, value)
            value["id"]?.asInt?.let { providedItemId ->
                itemId = providedItemId
            }
            idMap[itemId] = item
            nameMap[item.name] = item
        }
        initialized = true
    }

    fun unload() {
        nameMap.clear()
        idMap.clear()
    }
}
