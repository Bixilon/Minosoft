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

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.MultiResourceLocationAble
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.ResourceLocationAble
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

open class Registry<T : RegistryItem>(
    override var parent: AbstractRegistry<T>? = null,
) : AbstractRegistry<T> {
    private var initialized = false
    protected val idValueMap: Int2ObjectOpenHashMap<T> = Int2ObjectOpenHashMap()
    protected val valueIdMap: Object2IntOpenHashMap<T> = Object2IntOpenHashMap()
    protected val resourceLocationMap: MutableMap<ResourceLocation, T> = mutableMapOf()

    override val size: Int
        get() {
            val value = valueIdMap.size
            parent?.let {
                return value + it.size
            }
            return value
        }

    open operator fun get(json: JsonElement?): T? {
        return when (json) {
            null -> return null
            is JsonPrimitive -> {
                when {
                    json.isString -> get(json.asString.toResourceLocation())!!
                    json.isNumber -> get(json.asInt)
                    else -> TODO()
                }
            }
            else -> TODO()
        }
    }

    override operator fun get(any: Any?): T? {
        return when (any) {
            null -> null
            is Number -> get(any.toInt())
            is JsonElement -> get(any)
            is ResourceLocation -> get(any)
            is String -> get(any)
            is ResourceLocationAble -> get(any.resourceLocation)
            else -> TODO()
        }
    }

    open operator fun get(resourceLocation: ResourceLocation): T? {
        return resourceLocationMap[resourceLocation] ?: parent?.get(resourceLocation)
    }

    open operator fun set(any: Any, value: T) {
        when (any) {
            is Int -> {
                idValueMap[any] = value
                valueIdMap[value] = any
            }
            is ResourceLocation -> {
                resourceLocationMap[any] = value
            }
            is ResourceLocationAble -> {
                resourceLocationMap[any.resourceLocation] = value
            }
            is MultiResourceLocationAble -> {
                for (resourceLocation in any.ALIASES) {
                    resourceLocationMap[resourceLocation] = value
                }
            }
            else -> TODO()
        }
    }

    open operator fun get(resourceLocation: String): T? {
        return get(resourceLocation.toResourceLocation())
    }

    open operator fun get(resourceLocation: ResourceLocationAble): T? {
        return get(resourceLocation.resourceLocation)
    }

    override operator fun get(id: Int): T {
        return idValueMap[id] ?: parent?.get(id) ?: throw NullPointerException("Can not find item with id $id")
    }

    override fun getId(value: T): Int {
        return valueIdMap[value] ?: parent?.getId(value)!!
    }

    open fun initialize(data: Map<ResourceLocation, Any>?, registries: Registries?, deserializer: ResourceLocationDeserializer<T>, flattened: Boolean = true, metaType: MetaTypes = MetaTypes.NONE, alternative: Registry<T>? = null): Registry<T> {
        check(!initialized) { "Already initialized" }

        if (data == null) {
            if (alternative != null) {
                parent = alternative
            }
            return this
        }

        for ((resourceLocation, value) in data) {
            check(value is Map<*, *>)
            val item = deserializer.deserialize(registries, resourceLocation, value.asJsonObject()) ?: continue
            value["id"]?.toInt()?.let { id ->
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
                    value["meta"]?.toInt()?.let { meta ->
                        itemId = itemId or meta
                    }
                }
                idValueMap[id] = item
                valueIdMap[item] = id
            }
            resourceLocationMap[resourceLocation] = item
        }
        if (resourceLocationMap.isEmpty()) {
            parent = alternative
        }
        initialized = true
        return this
    }

    open fun rawInitialize(data: Map<String, Any>?, registries: Registries?, deserializer: ResourceLocationDeserializer<T>, flattened: Boolean = true, metaType: MetaTypes = MetaTypes.NONE, alternative: Registry<T>? = null): Registry<T> {
        return initialize(data?.toResourceLocationMap(), registries, deserializer, flattened, metaType, alternative)
    }

    open fun postInit(registries: Registries) {
        for ((_, value) in resourceLocationMap) {
            value.inject(registries)
            value.postInit(registries)
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

    override fun noParentIterator(): Iterator<T> {
        return resourceLocationMap.values.iterator()
    }
}
