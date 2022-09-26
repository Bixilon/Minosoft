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
package de.bixilon.minosoft.data.registries.blocks

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.VoxelShape
import de.bixilon.minosoft.data.registries.blocks.light.DirectedProperty
import de.bixilon.minosoft.data.registries.blocks.light.LightProperties
import de.bixilon.minosoft.data.registries.blocks.light.SolidProperty
import de.bixilon.minosoft.data.registries.blocks.light.TransparentProperty
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.materials.Material
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel
import java.util.*

data class BlockState(
    val block: Block,
    val properties: Map<BlockProperties, Any> = emptyMap(),
    val material: Material,
    val collisionShape: VoxelShape,
    val outlineShape: VoxelShape,
    val hardness: Float,
    val requiresTool: Boolean,
    val isSolid: Boolean,
    val luminance: Int,
    val lightProperties: LightProperties,
) {
    var blockModel: BakedBlockModel? = null

    override fun hashCode(): Int {
        return Objects.hash(block, properties)
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
            return block.resourceLocation.path == other.block.resourceLocation.path && properties == other.properties && block.resourceLocation.namespace == other.block.resourceLocation.namespace
        }
        if (other is ResourceLocation) {
            return super.equals(other)
        }
        return false
    }

    override fun toString(): String {
        val out = StringBuilder()
        out.append(block.resourceLocation.toString())
        out.append(" (")
        if (properties.isNotEmpty()) {
            out.append("properties=")
            out.append(properties)
        }
        if (out.isNotEmpty()) {
            out.append(")")
        }
        return out.toString().removeSuffix("()")
    }

    companion object {

        fun deserialize(block: Block, registries: Registries, data: Map<String, Any>): BlockState {
            val properties = data["properties"]?.toJsonObject()?.let {
                getProperties(it)
            } ?: emptyMap()

            val material = registries.materialRegistry[ResourceLocation(data["material"].unsafeCast())]!!


            fun Any.asShape(): VoxelShape {
                return if (this is Int) {
                    registries.shapes[this]
                } else {
                    VoxelShape(registries.shapes, this)
                }
            }

            val collisionShape = data["collision_shape"]?.asShape()
                ?: if (data["is_collision_shape_full_block"]?.toBoolean() == true) {
                    VoxelShape.FULL
                } else {
                    VoxelShape.EMPTY
                }

            val outlineShape = data["outline_shape"]?.asShape() ?: VoxelShape.EMPTY


            val lightProperties = if (outlineShape == VoxelShape.EMPTY) {
                TransparentProperty
            } else if (outlineShape == VoxelShape.FULL) {
                if (data["is_opaque"]?.toBoolean() != false) SolidProperty else TransparentProperty
            } else {
                DirectedProperty.of(outlineShape)
            }


            return BlockState(
                block = block,
                properties = properties,
                material = material,
                collisionShape = collisionShape,
                outlineShape = outlineShape,
                hardness = data["hardness"]?.toFloat() ?: 1.0f,
                requiresTool = data["requires_tool"]?.toBoolean() ?: !material.soft,
                isSolid = data["solid_render"]?.toBoolean() ?: false,
                luminance = data["luminance"]?.toInt() ?: 0,
                lightProperties = lightProperties,
            )
        }

        private fun getProperties(json: Map<String, Any>): MutableMap<BlockProperties, Any> {
            val properties: MutableMap<BlockProperties, Any> = mutableMapOf()
            for ((propertyGroup, propertyJsonValue) in json) {
                val propertyValue: Any = when (propertyJsonValue) {
                    is String -> propertyJsonValue.lowercase(Locale.getDefault())
                    else -> propertyJsonValue
                }
                try {
                    val (blockProperty, value) = BlockProperties.parseProperty(propertyGroup, propertyValue)
                    properties[blockProperty] = value
                } catch (exception: NullPointerException) {
                    throw NullPointerException("Invalid block property $propertyGroup or value $propertyValue")
                }
            }
            return properties
        }
    }


    fun withProperties(vararg properties: Pair<BlockProperties, Any>): BlockState {
        return withProperties(properties.toMap())
    }

    fun withProperties(properties: Map<BlockProperties, Any>): BlockState {
        val newProperties = this.properties.toMutableMap()
        for ((key, value) in properties) {
            newProperties[key] = value
        }
        val wannabe = WannabeBlockState(resourceLocation = this.block.resourceLocation, properties = newProperties)
        for (blockState in this.block.states) {
            if (blockState.equals(wannabe)) {
                return blockState
            }
        }
        throw IllegalArgumentException("Can not find ${this.block.resourceLocation}, with properties: $properties")
    }


    fun cycle(property: BlockProperties): BlockState {
        val currentValue = properties[property] ?: throw IllegalArgumentException("$this has no property $property")

        return withProperties(property to block.properties[property]!!.next(currentValue))
    }

    private fun <T> List<T>.next(current: T): T {
        val index = this.indexOf(current)
        check(index >= 0) { "List does not contain $current" }

        if (index == this.size - 1) {
            return this[0]
        }
        return this[index + 1]
    }
}
