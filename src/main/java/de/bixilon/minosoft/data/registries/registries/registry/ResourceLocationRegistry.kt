package de.bixilon.minosoft.data.registries.registries.registry

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.json.ResourceLocationJsonMap.toResourceLocationMap

class ResourceLocationRegistry(
    override var parent: AbstractRegistry<ResourceLocation>? = null,
) : AbstractRegistry<ResourceLocation> {
    private var initialized = false
    private val idValueMap: MutableMap<Int, ResourceLocation> = mutableMapOf()
    private val valueIdMap: MutableMap<ResourceLocation, Int> = mutableMapOf()


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

    override fun get(any: Any?): ResourceLocation? {
        check(any is Int) { "Don't know how to get $any" }
        return idValueMap[any]
    }

    override fun get(id: Int): ResourceLocation? {
        return idValueMap[id]
    }

    override fun getId(value: ResourceLocation): Int {
        return valueIdMap[value] ?: -1
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

    @Deprecated("TODO")
    override fun iterator(): Iterator<ResourceLocation> {
        return idValueMap.values.iterator()
    }
}
