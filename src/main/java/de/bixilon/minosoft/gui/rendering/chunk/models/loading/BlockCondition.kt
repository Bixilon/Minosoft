/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.models.loading

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import de.bixilon.minosoft.data.mappings.blocks.BlockProperties
import de.bixilon.minosoft.data.mappings.blocks.BlockRotations
import de.bixilon.minosoft.data.mappings.blocks.BlockState

open class BlockCondition {
    private var blockProperties: MutableList<MutableList<MutableSet<BlockProperties>>> = mutableListOf() // in order of OR AND OR
    private var rotation: BlockRotations = BlockRotations.NONE

    constructor(data: JsonElement) {
        when (data) {
            null -> return
            is JsonObject -> {
                addToProperties(data.asJsonObject)
            }
            is JsonArray -> {
                for (element in data.asJsonArray) {
                    addToProperties(element.asJsonObject)
                }
            }
        }
    }

    private fun addToProperties(data: JsonObject) {
        val current: MutableList<MutableSet<BlockProperties>> = mutableListOf()
        for ((propertyName, propertyJsonValue) in data.entrySet()) {
            check(propertyJsonValue is JsonPrimitive) { "Not a json primitive!" }
            val propertyValue: Any = when {
                propertyJsonValue.isBoolean -> {
                    propertyJsonValue.asBoolean
                }
                propertyJsonValue.isNumber -> {
                    propertyJsonValue.asInt
                }
                else -> {
                    // ToDo: Why is this needed?
                    try {
                        Integer.parseInt(propertyJsonValue.asString)
                    } catch (exception: Exception) {
                        propertyJsonValue.asString.toLowerCase()
                    }
                }
            }
            try {
                if (propertyName in BlockState.ROTATION_PROPERTIES) {
                    rotation = BlockRotations.ROTATION_MAPPING[propertyValue]!!
                } else {
                    BlockProperties.PROPERTIES_MAPPING[propertyName]?.get(propertyValue)?.let {
                        current.add(mutableSetOf(it))
                    } ?: kotlin.run {
                        if (propertyValue is String) {
                            val propertyString: String = propertyValue
                            if (propertyString.contains("|")) {
                                val parts = propertyString.split("|")
                                val properties = mutableSetOf<BlockProperties>()
                                for (part in parts) {
                                    properties.add(BlockProperties.PROPERTIES_MAPPING[propertyName]!![part]!!)
                                }
                                current.add(properties)
                            }
                        }
                    }
                }
            } catch (exception: NullPointerException) {
                throw NullPointerException("Invalid block property $propertyName with value $propertyValue")
            }
        }
        blockProperties.add(current)
    }

    constructor()

    open fun contains(testProperties: MutableSet<BlockProperties>, testRotation: BlockRotations): Boolean {
        if (rotation != BlockRotations.NONE && rotation != testRotation) {
            return false
        }
        for (propertiesSubSet in blockProperties) {
            var propertiesGood = true
            for (properties in propertiesSubSet) {
                for (property in properties) {
                    if (! testProperties.contains(property)) {
                        propertiesGood = false
                        break
                    }
                }
                if (! propertiesGood) {
                    break
                }
            }
            if (propertiesGood) {
                return true
            }
        }
        return false
    }

    companion object {
        val TRUE_CONDITION: BlockCondition = object : BlockCondition() {
            override fun contains(testProperties: MutableSet<BlockProperties>, testRotation: BlockRotations): Boolean {
                return true
            }
        }
    }
}
