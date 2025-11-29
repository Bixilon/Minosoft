/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.blocks.state.builder

import de.bixilon.kutil.enums.inline.IntInlineSet
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.blocks.light.*
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty
import de.bixilon.minosoft.data.registries.blocks.properties.list.BlockPropertyList
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.building.fence.FenceBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.legacy.LegacyBlock
import de.bixilon.minosoft.data.registries.blocks.types.pixlyzer.PixLyzerBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.shapes.ShapeRegistry
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.registries.shapes.shape.Shape.Companion.deserialize

class BlockStateBuilder(
    val properties: Map<BlockProperty<*>, Any>,
    val luminance: Int,
    val collisionShape: Shape?,
    val outlineShape: Shape?,
    val lightProperties: LightProperties,
    val flags: IntInlineSet,
) {

    fun build(block: Block) = BlockState(block, properties, flags, luminance, collisionShape, outlineShape, lightProperties)

    fun build(block: Block, luminance: Int = this.luminance, collisionShape: Shape? = this.collisionShape, outlineShape: Shape? = this.outlineShape, lightProperties: LightProperties = this.lightProperties, flags: IntInlineSet = this.flags): BlockState {
        return BlockState(block, properties, flags, luminance, collisionShape, outlineShape, lightProperties)
    }

    companion object {

        private fun Any.getFromShapes(shapes: ShapeRegistry) = shapes[this]

        private fun JsonObject.getCollisionShape(shapes: ShapeRegistry): Shape? {
            this["is_collision_shape_full_block"]?.toBoolean()?.let { if (it) return Shape.FULL else null }

            this["collision_shape"]?.getFromShapes(shapes)?.let { return it }

            return null
        }

        private fun JsonObject.getOutlineShape(shapes: ShapeRegistry): Shape? {
            this["outline_shape"]?.getFromShapes(shapes)?.let { return it }

            return null
        }

        private fun JsonObject.getProperties(list: BlockPropertyList?): Map<BlockProperty<*>, Any>? {
            val data = this["properties"]?.toJsonObject() ?: return null
            if (data.isEmpty()) return null

            val properties: MutableMap<BlockProperty<*>, Any> = HashMap(data.size)

            for ((group, json) in data) {
                try {
                    val raw = if (json is String) json.lowercase() else json
                    var property: BlockProperty<*>
                    var value: Any

                    val listProperty = list?.get(group)
                    if (listProperty == null) {
                        val pair = BlockProperties.parseFallbackProperty(group, raw)
                        property = pair.first; value = pair.second
                    } else {
                        property = listProperty; value = listProperty.parse(raw)!!
                    }

                    properties[property] = value
                } catch (exception: NullPointerException) {
                    throw NullPointerException("Invalid block property $group or value $json")
                }
            }

            return properties
        }

        private fun JsonObject.getLuminance(): Int {
            return this["luminance"]?.toInt() ?: 0
        }

        private fun JsonObject.getLightProperties(outlineShape: Shape?): LightProperties {
            val opaque = this["is_opaque"]?.toBoolean() ?: true
            val translucent = this["translucent"]?.toBoolean() ?: true

            var lightProperties = when {
                outlineShape == null || (!opaque && translucent) -> TransparentProperty
                outlineShape == Shape.FULL -> OpaqueProperty
                else -> DirectedProperty.of(outlineShape, opaque, false)
            }

            if (lightProperties is OpaqueProperty && !opaque) {
                lightProperties = CustomLightProperties(propagatesLight = true, skylightEnters = false, !translucent)
            }

            return lightProperties
        }

        fun of(block: Block, flags: IntInlineSet, registries: Registries, data: JsonObject): BlockStateBuilder {
            val implemented = !(block is PixLyzerBlock || block is LegacyBlock || block is FenceBlock)
            val properties = data.getProperties(block.properties) ?: emptyMap()
            val collisionShape = if (!implemented) data.getCollisionShape(registries.shape) else null
            val outlineShape = if (!implemented) data.getOutlineShape(registries.shape) else null


            var flags = BlockStateFlags.update(flags, properties)


            if (!implemented && data["solid_render"]?.toBoolean() == true) flags += BlockStateFlags.FULL_OPAQUE


            val lightProperties = when {
                BlockStateFlags.FULL_OPAQUE in flags -> OpaqueProperty
                BlockStateFlags.WATERLOGGED in flags -> FluidBlock.LIGHT_PROPERTIES
                !implemented -> data.getLightProperties(outlineShape)
                BlockStateFlags.OUTLINE in flags && block is OutlinedBlock -> data.getLightProperties(block.outlineShape) // TODO: get outline shape by block state?
                else -> TransparentProperty
            }

            return BlockStateBuilder(
                properties = properties,
                luminance = data.getLuminance(),
                collisionShape = collisionShape,
                outlineShape = outlineShape,
                lightProperties = lightProperties,
                flags = flags,
            )
        }
    }
}
