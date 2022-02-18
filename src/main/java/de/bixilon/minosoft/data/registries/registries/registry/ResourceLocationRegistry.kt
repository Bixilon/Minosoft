/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

class ResourceLocationRegistry(
    override var parent: AbstractRegistry<ResourceLocation>? = null,
) : AbstractRegistry<ResourceLocation> {
    private var initialized = false
    private val idValueMap: Int2ObjectOpenHashMap<ResourceLocation> = Int2ObjectOpenHashMap()
    private val valueIdMap: Object2IntOpenHashMap<ResourceLocation> = Object2IntOpenHashMap()


    override val size: Int
        get() {
            val value = idValueMap.size
            parent?.let {
                return value + it.size
            }
            return value
        }

    override fun clear() {
        idValueMap.clear()
        valueIdMap.clear()
    }

    override operator fun get(any: Any?): ResourceLocation? {
        check(any is Int) { "Don't know how to get $any" }
        return this[any]
    }

    override operator fun get(id: Int): ResourceLocation? {
        return idValueMap[id] ?: parent?.get(id)
    }

    override fun getId(value: ResourceLocation): Int {
        return valueIdMap[value] ?: parent?.getId(value) ?: -1
    }

    fun initialize(data: Map<ResourceLocation, Any>?, alternative: ResourceLocationRegistry? = null): ResourceLocationRegistry {
        check(!initialized) { "Already initialized" }

        if (data == null) {
            if (alternative != null) {
                parent = alternative
            }
            return this
        }

        for ((resourceLocation, value) in data) {
            val id: Int = when (value) {
                is Number -> value.toInt()
                is Map<*, *> -> value["id"].toInt()
                else -> throw IllegalArgumentException("Don't know what $value is!")
            }
            idValueMap[id] = resourceLocation
            valueIdMap[resourceLocation] = id
        }
        if (idValueMap.isEmpty()) {
            parent = alternative
        }
        initialized = true
        return this
    }

    fun rawInitialize(data: Map<String, Any>?, alternative: ResourceLocationRegistry? = null): ResourceLocationRegistry {
        return initialize(data?.toResourceLocationMap(), alternative)
    }


    override fun toString(): String {
        return super.toString() + ": ${idValueMap.size}x"
    }

    override fun noParentIterator(): Iterator<ResourceLocation> {
        return idValueMap.values.iterator()
    }
}
