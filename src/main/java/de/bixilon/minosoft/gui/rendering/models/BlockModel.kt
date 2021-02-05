package de.bixilon.minosoft.gui.rendering.models

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.world.InChunkSectionLocation

open class BlockModel(val parent: BlockModel? = null, textureIndices: MutableMap<String, Int>, json: JsonObject) {
    private val textures: MutableMap<String, String> = parent?.textures?.toMutableMap() ?: mutableMapOf()
    private val textureIndexMap: MutableMap<String, Int> = parent?.textureIndexMap?.toMutableMap() ?: mutableMapOf()
    private var elements: MutableList<BlockModelElement> = parent?.elements?.toMutableList() ?: mutableListOf(BlockModelElement(JsonObject()))
    var full = true

    init {
        json["textures"]?.asJsonObject?.let {
            for ((type, value) in it.entrySet()) {
                textures[type] = value.asString
            }
        }
        for ((type, texture) in textures) {
            getTextureByType(texture).let {
                textures[type] = it
                if (!it.startsWith("#")) {
                    var index: Int? = textureIndices[it]
                    if (index == null) {
                        index = textureIndices.size
                        textureIndices[it] = index
                    }
                    textureIndexMap[type] = index
                }
            }
        }
        json["elements"]?.let { it ->
            elements.clear()
            for (element in it.asJsonArray) {
                elements.add(BlockModelElement(element.asJsonObject))
            }
        }
    }


    open fun render(position: InChunkSectionLocation, direction: Directions): List<Float> {
        val data: MutableList<Float> = mutableListOf()

        for (element in elements) {
            data.addAll(element.render(textureIndexMap, position, direction))
        }
        return data
    }


    private fun getTextureByType(type: String): String {
        var currentValue: String = type

        while (currentValue.startsWith("#")) {
            textures[currentValue.removePrefix("#")].let {
                if (it == null) {
                    return currentValue
                }
                currentValue = it
            }
        }

        return currentValue
    }


    companion object {
        fun positionToFloat(uv: Float): Float {
            return (uv - 8f) / 16f
        }
    }

}
