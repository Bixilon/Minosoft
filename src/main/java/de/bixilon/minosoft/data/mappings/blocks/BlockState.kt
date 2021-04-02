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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.mappings.materials.Material
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.TintColorCalculator
import de.bixilon.minosoft.gui.rendering.chunk.VoxelShape
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockRenderInterface
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockRenderer
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.FluidRenderer
import glm_.vec3.Vec3i
import java.util.*
import kotlin.random.Random

data class BlockState(
    val owner: Block,
    val properties: Map<BlockProperties, Any> = mapOf(),
    val rotation: BlockRotations = BlockRotations.NONE,
    val renders: MutableList<BlockRenderInterface> = mutableListOf(),
    val tintColor: RGBColor? = null,
    val material: Material,
    val collisionShape: VoxelShape,
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
        if (other is WannabeBlockState) {
            if (owner.resourceLocation != other.resourceLocation) {
                return false
            }

            other.rotation?.let {
                if (rotation != it) {
                    return false
                }
            }
            other.properties?.let {
                for ((state, value) in it) {
                    if (properties[state] != value) {
                        return false
                    }
                }
            }

            return true
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

    fun getBlockRenderer(blockPosition: Vec3i): BlockRenderInterface {
        if (Minosoft.getConfig().config.game.other.antiMoirePattern && renders.size > 1) {
            // ToDo: Support weight attribute
            return renders.random(Random(blockPosition.hashCode()))
        }
        return renders[0]
    }


    companion object {
        val ROTATION_PROPERTIES = setOf("facing", "rotation", "orientation", "axis")

        private val SPECIAL_RENDERERS = mutableMapOf(
            "water" to FluidRenderer("block/water_still", "block/water_flow", "water"),
            "lava" to FluidRenderer("block/lava_still", "block/lava_flow", "lava"),
        ) // ToDo: Don't like this

        fun deserialize(owner: Block, versionMapping: VersionMapping, data: JsonObject, models: Map<ResourceLocation, BlockModel>): BlockState {
            val (rotation, properties) = data["properties"]?.asJsonObject?.let {
                getProperties(it)
            } ?: Pair(BlockRotations.NONE, mutableMapOf())
            val renders: MutableList<BlockRenderInterface> = mutableListOf()

            data["render"]?.let {
                when (it) {
                    is JsonArray -> {
                        for (model in it) {
                            check(model is JsonObject)
                            addBlockModel(model, renders, models)
                        }
                    }
                    is JsonObject -> {
                        addBlockModel(it.asJsonObject, renders, models)
                    }
                    else -> error("Not a render json!")
                }
            }

            owner.multipartMapping?.let {
                val elementRenderers: MutableList<JsonObject> = mutableListOf()
                for ((condition, model) in it.entries) {
                    if (condition.contains(properties, rotation)) {
                        elementRenderers.addAll(model)
                    }
                }
                renders.add(BlockRenderer(elementRenderers, models))
            }

            val tintColor: RGBColor? = data["tint_color"]?.asInt?.let { TintColorCalculator.getJsonColor(it) } ?: owner.tintColor

            for ((regex, renderer) in SPECIAL_RENDERERS) {
                if (owner.resourceLocation.full.contains(regex)) {
                    renders.clear()
                    renders.add(renderer)
                }
            }

            val material = versionMapping.materialRegistry.get(ResourceLocation(data["material"].asString))!!

            val collision = data["collision_shapes"]?.let {
                versionMapping.shapes[it.asInt]
            } ?: if (data["is_collision_shape_full_block"]?.asBoolean == true) {
                VoxelShape.FULL
            } else {
                VoxelShape.EMPTY
            }

            return BlockState(
                owner = owner,
                properties = properties.toMap(),
                rotation = rotation,
                renders = renders,
                tintColor = tintColor,
                material = material,
                collisionShape = collision
            )
        }

        private fun getProperties(json: JsonObject): Pair<BlockRotations, MutableMap<BlockProperties, Any>> {
            var rotation = BlockRotations.NONE
            val properties: MutableMap<BlockProperties, Any> = mutableMapOf()
            for ((propertyGroup, propertyJsonValue) in json.entrySet()) {
                check(propertyJsonValue is JsonPrimitive) { "Not a json primitive!" }
                val propertyValue: Any = when {
                    propertyJsonValue.isBoolean -> {
                        propertyJsonValue.asBoolean
                    }
                    propertyJsonValue.isNumber -> {
                        propertyJsonValue.asInt
                    }
                    else -> {
                        propertyJsonValue.asString.toLowerCase()
                    }
                }
                try {
                    if (propertyGroup in ROTATION_PROPERTIES) {
                        rotation = BlockRotations.ROTATION_MAPPING[propertyValue]!!
                    } else {
                        val (blockProperty, value) = BlockProperties.parseProperty(propertyGroup, propertyValue)
                        properties[blockProperty] = value
                    }
                } catch (exception: NullPointerException) {
                    throw NullPointerException("Invalid block property $propertyGroup or value $propertyValue")
                }
            }
            return Pair(rotation, properties)
        }

        private fun addBlockModel(data: JsonObject, renders: MutableList<BlockRenderInterface>, models: Map<ResourceLocation, BlockModel>) {
            val model = models[ResourceLocation(data["model"].asString)] ?: error("Can not find block model ${data["model"]}")
            renders.add(BlockRenderer(data, model))
        }
    }

    // properties

    fun isPowered(): Boolean? {
        return properties[BlockProperties.POWERED] as Boolean?
    }
    // ToDo
}
