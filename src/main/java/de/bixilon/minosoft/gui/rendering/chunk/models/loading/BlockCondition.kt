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
import de.bixilon.minosoft.data.mappings.blocks.BlockRotations
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.blocks.properties.BlockProperties

open class BlockCondition {
    private var blockProperties: MutableList<MutableList<MutableMap<BlockProperties, Any>>> = mutableListOf() // in order of OR AND OR
    private var rotation: BlockRotations = BlockRotations.NONE

    constructor(data: JsonElement) {
        when (data) {
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

    constructor()

    private fun addToProperties(data: JsonObject) {
        val current: MutableList<MutableMap<BlockProperties, Any>> = mutableListOf()
        for ((groupName, propertyJsonValue) in data.entrySet()) {
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
                if (groupName in BlockState.ROTATION_PROPERTIES) {
                    rotation = BlockRotations.ROTATION_MAPPING[propertyValue]!!
                } else {
                    try {
                        current.add(mutableMapOf(BlockProperties.parseProperty(groupName, propertyValue)))
                    } catch (exception: Throwable) {
                        if (propertyValue is String) {
                            val propertyString: String = propertyValue
                            if (propertyString.contains("|")) {
                                val parts = propertyString.split("|")
                                val properties: MutableMap<BlockProperties, Any> = mutableMapOf()
                                for (part in parts) {
                                    val (property, value) = BlockProperties.parseProperty(groupName, part)
                                    properties[property] = value
                                }
                                current.add(properties)
                            }
                        }
                    }
                }
            } catch (exception: NullPointerException) {
                throw NullPointerException("Invalid block property $groupName with value $propertyValue")
            }
        }
        blockProperties.add(current)
    }

    open fun contains(testProperties: Map<BlockProperties, Any>, testRotation: BlockRotations): Boolean {
        if (rotation != BlockRotations.NONE && rotation != testRotation) {
            return false
        }
        outerLoop@ for (propertiesSubSet in blockProperties) {
            for (properties in propertiesSubSet) {
                if (testProperties.keys.intersect(properties.keys).isEmpty()) { // ToDo: Just keys or also values???
                    continue@outerLoop
                }
            }
            return true
        }
        return false
    }

    companion object {
        val TRUE_CONDITION: BlockCondition = object : BlockCondition() {
            override fun contains(testProperties: Map<BlockProperties, Any>, testRotation: BlockRotations): Boolean {
                return true
            }
        }
    }
}
