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

package de.bixilon.minosoft.data.registries.registry

import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.collections.Clearable
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound

class FakeEnumRegistry<T : RegistryFakeEnumerable>(
    override var parent: FakeEnumRegistry<T>? = null,
) : Clearable, Parentable<FakeEnumRegistry<T>> {
    private var initialized = false
    private val idValueMap: MutableMap<Int, T> = mutableMapOf()
    private val valueIdMap: MutableMap<T, Int> = mutableMapOf()
    private val nameValueMap: MutableMap<String, T> = mutableMapOf()

    operator fun get(name: String): T? {
        return nameValueMap[name] ?: parent?.get(name)
    }

    operator fun get(id: Int): T? {
        return idValueMap[id] ?: parent?.get(id)
    }

    fun getId(value: T): Int {
        return valueIdMap[value] ?: parent?.getId(value)!!
    }

    fun initialize(data: Map<Any, Any>?, registries: Registries, deserializer: IdDeserializer<T>): FakeEnumRegistry<T> {
        check(!initialized) { "Already initialized" }

        if (data == null) {
            return this
        }

        for ((id, value) in data) {
            check(value is Map<*, *>)
            var itemId = id.toInt()

            val item = deserializer.deserialize(registries, value.asCompound())
            value["id"]?.toInt()?.let { providedItemId ->
                itemId = providedItemId
            }
            idValueMap[itemId] = item
            valueIdMap[item] = itemId
            nameValueMap[item.name] = item
        }
        initialized = true
        return this
    }

    override fun clear() {
        idValueMap.clear()
        valueIdMap.clear()
        nameValueMap.clear()
    }
}
