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

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import de.bixilon.minosoft.util.collections.Clearable
import de.bixilon.minosoft.util.enum.ValuesEnum

class EnumRegistry<T : Enum<*>>(
    private var parentRegistry: EnumRegistry<T>? = null,
    var values: ValuesEnum<T>,
    private val mutable: Boolean = true,
) : Clearable, Parentable<EnumRegistry<T>> {
    private var initialized = false
    private val idValueMap: MutableMap<Int, T> = mutableMapOf()
    private val valueIdMap: MutableMap<T, Int> = mutableMapOf()

    fun get(id: Int): T? {
        return idValueMap[id] ?: parentRegistry?.get(id)
    }

    fun getId(value: T): Int {
        return valueIdMap[value] ?: parentRegistry?.getId(value)!!
    }

    override fun setParent(parent: EnumRegistry<T>?) {
        if (!mutable) {
            throw IllegalStateException("Registry is immutable!")
        }
        this.parentRegistry = parent
    }

    private fun getEnum(data: Any): T {
        return when (data) {
            is Int -> values[data]
            is String -> values.NAME_MAP[data.toLowerCase()] ?: error("Can not find enum: $data")
            is JsonPrimitive -> {
                if (data.isNumber) {
                    return getEnum(data.asInt)
                }
                return getEnum(data.asString)
            }
            else -> throw IllegalArgumentException("Unknown enum value: $data")
        }
    }

    private fun putEnum(data: JsonElement, alternativeIndex: Int) {
        val id: Int
        val value: T
        when (data) {
            is JsonObject -> {
                id = data["id"].asInt
                value = getEnum(data["value"])
            }
            is JsonPrimitive -> {
                id = alternativeIndex
                value = getEnum(data)
            }
            else -> throw IllegalArgumentException("Can not get enum value: $data")
        }
        idValueMap[id] = value
        valueIdMap[value] = id
    }

    fun initialize(data: JsonElement?): EnumRegistry<T> {
        check(!initialized) { "Already initialized" }

        if (data == null) {
            return this
        }

        when (data) {
            is JsonArray -> {
                for ((index, enum) in data.withIndex()) {
                    putEnum(enum, index)
                }
            }
            is JsonObject -> {
                for ((index, enum) in data.entrySet()) {
                    putEnum(enum, Integer.valueOf(index))
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
