/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.blocks

import com.google.common.collect.HashBiMap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.gui.rendering.TintColorCalculator
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockRenderer
import java.util.*
import kotlin.random.Random

data class BlockState(
    val owner: Block,
    val properties: Set<BlockProperties> = setOf(),
    val rotation: BlockRotations = BlockRotations.NONE,
    val renders: Set<BlockRenderer> = setOf(),
    val tintColor: RGBColor? = null,
) {

    override fun hashCode(): Int {
        return Objects.hash(owner, properties, rotation)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (hashCode() != other.hashCode()) {
            return false
        }
        if (other is BlockState) {
            return owner.resourceLocation == other.owner.resourceLocation && rotation == other.rotation && properties == other.properties && owner.resourceLocation.namespace == other.owner.resourceLocation.namespace
        }
        if (other is ResourceLocation) {
            return super.equals(other)
        }
        return false
    }

    fun bareEquals(obj: Any): Boolean {
        if (this === obj) {
            return true
        }
        if (obj is BlockState) {
            if (owner.resourceLocation.namespace != obj.owner.resourceLocation.namespace || owner.resourceLocation.path != obj.owner.resourceLocation.path) {
                return false
            }
            if (obj.rotation != BlockRotations.NONE) {
                if (obj.rotation != rotation) {
                    return false
                }
            }
            for (property in obj.properties) {
                if (!properties.contains(property)) {
                    return false
                }
            }
            return true
        }
        return if (obj is ResourceLocation) {
            super.equals(obj)
        } else false
    }

    override fun toString(): String {
        val out = StringBuilder()
        if (rotation != BlockRotations.NONE) {
            out.append(" (")
            out.append("rotation=")
            out.append(rotation)
        }
        if (properties.isNotEmpty()) {
            if (out.isNotEmpty()) {
                out.append(", ")
            } else {
                out.append(" (")
            }
            out.append("properties=")
            out.append(properties)
        }
        if (out.isNotEmpty()) {
            out.append(")")
        }
        return String.format("%s%s", owner.resourceLocation, out)
    }

    fun getBlockRenderer(position: BlockPosition): BlockRenderer {
        if (Minosoft.getConfig().config.game.other.antiMoirePattern) {
            // ToDo: Support weight attribute
            return renders.random(Random(position.hashCode()))
        }
        return renders.iterator().next()
    }


    companion object {
        private val ROTATION_PROPERTIES = setOf("facing", "rotation", "orientation", "axis")

        fun deserialize(owner: Block, data: JsonObject, models: HashBiMap<ResourceLocation, BlockModel>): BlockState {
            var rotation: BlockRotations = BlockRotations.NONE
            val properties: MutableSet<BlockProperties> = mutableSetOf()

            data["properties"]?.asJsonObject?.let {
                for ((propertyName, propertyJsonValue) in it.entrySet()) {
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
                        if (propertyName in ROTATION_PROPERTIES) {
                            rotation = BlockRotations.ROTATION_MAPPING[propertyValue]!!
                        } else {
                            properties.add(BlockProperties.PROPERTIES_MAPPING[propertyName]!![propertyValue]!!)
                        }
                    } catch (exception: NullPointerException) {
                        throw NullPointerException("Invalid block property $propertyName or value $propertyValue")
                    }
                }
            }
            val renders: MutableSet<BlockRenderer> = mutableSetOf()

            data["render"]?.let {
                when (it) {
                    is JsonArray -> {
                        for (model in it) {
                            check(model is JsonObject)

                            addBlockModel(model, renders, models)
                        }
                    }
                    is JsonObject -> {
                        addBlockModel(it, renders, models)
                    }
                    else -> error("Not a render json!")
                }
            }

            val tintColor: RGBColor? = data["tint_color"]?.asInt?.let { TintColorCalculator.getColor(it) } ?: owner.tintColor


            return BlockState(
                owner = owner,
                properties = properties.toSet(),
                rotation = rotation,
                renders = renders.toSet(),
                tintColor = tintColor
            )
        }

        private fun addBlockModel(data: JsonObject, renders: MutableSet<BlockRenderer>, models: HashBiMap<ResourceLocation, BlockModel>) {
            val model = models[ResourceLocation(data["model"].asString)] ?: error("Can not find block model ${data["model"]}")

            renders.add(BlockRenderer(data, model))
        }
    }
}
