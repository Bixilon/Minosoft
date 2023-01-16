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

import de.bixilon.kutil.collections.primitive.Clearable
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.codec.IdCodec
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

class FakeEnumRegistry<T : RegistryFakeEnumerable>(
    override var parent: FakeEnumRegistry<T>? = null,
    val codec: IdCodec<T>,
) : Clearable, Parentable<FakeEnumRegistry<T>> {
    private val idValueMap: Int2ObjectOpenHashMap<T> = Int2ObjectOpenHashMap()
    private val valueIdMap: Object2IntOpenHashMap<T> = Object2IntOpenHashMap()
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

    fun update(data: Map<Any, Any>?, registries: Registries) {
        if (data == null) {
            return
        }

        for ((id, value) in data) {
            value as JsonObject
            var itemId = id.toInt()

            val item = codec.deserialize(registries, value)
            value["id"]?.toInt()?.let { itemId = it }

            idValueMap[itemId] = item
            valueIdMap[item] = itemId
            nameValueMap[item.name] = item
        }
    }

    override fun clear() {
        idValueMap.clear()
        valueIdMap.clear()
        nameValueMap.clear()
    }
}
