/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockLikeRenderer
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.BlockRenderer
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.MultipartRenderer
import glm_.vec3.Vec3i
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

data class BlockState(
    val block: Block,
    val properties: Map<BlockProperties, Any> = mapOf(),
    val rotation: BlockRotations = BlockRotations.NONE,
    val renderers: MutableList<BlockLikeRenderer> = mutableListOf(),
    val tintColor: RGBColor? = null,
    val material: Material,
    val collisionShape: VoxelShape,
) {

    override fun hashCode(): Int {
        return Objects.hash(block, properties, rotation)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other is WannabeBlockState) {
            if (block.resourceLocation != other.resourceLocation) {
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
            return block.resourceLocation == other.block.resourceLocation && rotation == other.rotation && properties == other.properties && block.resourceLocation.namespace == other.block.resourceLocation.namespace
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
        return String.format("%s%s", block.resourceLocation, out)
    }

    fun getBlockRenderer(blockPosition: Vec3i): BlockLikeRenderer {
        if (renderers.isEmpty()) {
            throw IllegalArgumentException("$this has not renderer!")
        }
        if (renderers.size == 1 || !Minosoft.getConfig().config.game.other.antiMoirePattern) {
            return renderers[0]
        }
        val random = Random(getPositionSeed(blockPosition.x, blockPosition.y, blockPosition.z))
        return renderers[abs(random.nextLong().toInt() % renderers.size)]
    }


    companion object {
        val ROTATION_PROPERTIES = setOf("facing", "rotation", "orientation", "axis")

        fun deserialize(owner: Block, versionMapping: VersionMapping, data: JsonObject, models: Map<ResourceLocation, BlockModel>): BlockState {
            val (rotation, properties) = data["properties"]?.asJsonObject?.let {
                getProperties(it)
            } ?: Pair(BlockRotations.NONE, mutableMapOf())

            val renderers: MutableList<BlockLikeRenderer> = mutableListOf()

            data["render"]?.let {
                when (it) {
                    is JsonArray -> {
                        for (model in it) {
                            when (model) {
                                is JsonObject -> {
                                    addBlockModel(model, renderers, models)
                                }
                                is JsonArray -> {
                                    val modelList: MutableList<BlockLikeRenderer> = mutableListOf()
                                    for (singleModel in model) {
                                        check(singleModel is JsonObject)
                                        addBlockModel(singleModel, modelList, models)
                                    }
                                    renderers.add(MultipartRenderer(modelList.toList()))
                                }
                            }
                        }
                    }
                    is JsonObject -> {
                        addBlockModel(it, renderers, models)
                    }
                    else -> error("Not a render json!")
                }
            }

            val tintColor: RGBColor? = data["tint_color"]?.asInt?.let { TintColorCalculator.getJsonColor(it) } ?: owner.tintColor


            val material = versionMapping.materialRegistry.get(ResourceLocation(data["material"].asString))!!

            val collision = data["collision_shapes"]?.let {
                versionMapping.shapes[it.asInt]
            } ?: if (data["is_collision_shape_full_block"]?.asBoolean == true) {
                VoxelShape.FULL
            } else {
                VoxelShape.EMPTY
            }

            owner.renderOverride?.let {
                renderers.clear()
                renderers.addAll(it)
            }

            return BlockState(
                block = owner,
                properties = properties.toMap(),
                rotation = rotation,
                renderers = renderers,
                tintColor = tintColor,
                material = material,
                collisionShape = collision,
            )
        }

        fun getPositionSeed(x: Int, y: Int, z: Int): Long {
            var ret = (x * 3129871L) xor z * 116129781L xor y.toLong()
            ret = ret * ret * 42317861L + ret * 11L
            return ret shr 16
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

        private fun addBlockModel(data: JsonObject, renderer: MutableList<BlockLikeRenderer>, models: Map<ResourceLocation, BlockModel>) {
            val model = models[ResourceLocation(data["model"].asString)] ?: error("Can not find block model ${data["model"]}")
            renderer.add(BlockRenderer(data, model))
        }
    }

    // properties

    fun isPowered(): Boolean? {
        return properties[BlockProperties.POWERED] as Boolean?
    }
    // ToDo
}
