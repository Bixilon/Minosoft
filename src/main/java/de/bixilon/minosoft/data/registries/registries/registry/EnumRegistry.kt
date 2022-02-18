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

package de.bixilon.minosoft.data.registries.registries.registry

import com.google.gson.JsonPrimitive
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.util.collections.Clearable
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import java.util.*

class EnumRegistry<T : Enum<*>>(
    override var parent: EnumRegistry<T>? = null,
    var values: ValuesEnum<T>,
    private val mutable: Boolean = true,
) : Clearable, Parentable<EnumRegistry<T>> {

    private var initialized = false
    private val idValueMap: Int2ObjectOpenHashMap<T> = Int2ObjectOpenHashMap()
    private val valueIdMap: Object2IntOpenHashMap<T> = Object2IntOpenHashMap()

    operator fun get(id: Int): T? {
        return idValueMap[id] ?: parent?.get(id)
    }

    fun getId(value: T): Int {
        return valueIdMap[value] ?: parent?.getId(value)!!
    }

    private fun getEnum(data: Any): T {
        return when (data) {
            is Int -> values[data]
            is String -> values.NAME_MAP[data.lowercase(Locale.getDefault())] ?: error("Can not find enum: $data")
            is JsonPrimitive -> {
                if (data.isNumber) {
                    return getEnum(data.asInt)
                }
                return getEnum(data.asString)
            }
            else -> throw IllegalArgumentException("Unknown enum value: $data")
        }
    }

    private fun putEnum(data: Any, alternativeIndex: Int) {
        val id: Int
        val value: T
        when (data) {
            is Map<*, *> -> {
                id = data["id"].unsafeCast()
                value = getEnum(data["value"]!!)
            }
            is String -> {
                id = alternativeIndex
                value = getEnum(data)
            }
            else -> throw IllegalArgumentException("Can not get enum value: $data")
        }
        idValueMap[id] = value
        valueIdMap[value] = id
    }

    fun initialize(data: Any?): EnumRegistry<T> {
        check(!initialized) { "Already initialized" }

        data ?: return this

        when (data) {
            is List<*> -> {
                for ((index, enum) in data.withIndex()) {
                    putEnum(enum!!, index)
                }
            }
            is Map<*, *> -> {
                for ((index, enum) in data) {
                    putEnum(enum!!, Integer.valueOf(index.unsafeCast<String>()))
                }
            }
            else -> throw IllegalArgumentException("Can not get enum value: $data")
        }

        initialized = true
        return this
    }

    override fun clear() {
        if (!mutable) {
            throw IllegalStateException("Registry is immutable!")
        }
        idValueMap.clear()
        valueIdMap.clear()
    }
}
