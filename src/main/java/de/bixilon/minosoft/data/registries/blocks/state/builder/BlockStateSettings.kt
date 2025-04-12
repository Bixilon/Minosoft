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

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.blocks.light.*
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty
import de.bixilon.minosoft.data.registries.blocks.properties.list.BlockPropertyList
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.shapes.ShapeRegistry
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.registries.shapes.shape.Shape.Companion.deserialize

class BlockStateSettings(
    val properties: Map<BlockProperty<*>, Any>?,
    val luminance: Int,
    var collisionShape: Shape?,
    var outlineShape: Shape?,
    val lightProperties: LightProperties,
    @Deprecated("pixlyzer") val solidRenderer: Boolean,
) {

    companion object {

        private fun Any.getShape(shapes: ShapeRegistry): Shape? {
            if (this is Int) {
                return shapes[this]
            }
            return shapes.deserialize(this)
        }

        private fun JsonObject.getCollisionShape(shapes: ShapeRegistry): Shape? {
            this["collision_shape"]?.getShape(shapes)?.let { return it }

            this["is_collision_shape_full_block"]?.toBoolean()?.let { if (it) return Shape.FULL else null }

            return null
        }

        private fun JsonObject.getOutlineShape(shapes: ShapeRegistry): Shape? {
            this["outline_shape"]?.getShape(shapes)?.let { return it }

            return null
        }

        private fun JsonObject.getProperties(list: BlockPropertyList?, block: Block): Map<BlockProperty<*>, Any>? {
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
                        val pair = BlockProperties.parseProperty(block, group, raw)
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

            var lightProperties = if (outlineShape == null || (!opaque && translucent)) {
                TransparentProperty
            } else if (outlineShape == Shape.FULL) {
                OpaqueProperty
            } else {
                DirectedProperty.of(outlineShape, opaque, !translucent)
            }


            if (lightProperties is OpaqueProperty && !opaque) {
                lightProperties = CustomLightProperties(propagatesLight = true, skylightEnters = false, !translucent)
            }
            return lightProperties
        }

        fun of(block: Block, properties: BlockPropertyList?, registries: Registries, data: JsonObject): BlockStateSettings {
            val collisionShape = data.getCollisionShape(registries.shape)
            val outlineShape = data.getOutlineShape(registries.shape)


            return BlockStateSettings(
                properties = data.getProperties(properties, block),
                luminance = data.getLuminance(),
                collisionShape = collisionShape,
                outlineShape = outlineShape,
                lightProperties = data.getLightProperties(outlineShape),
                solidRenderer = data["solid_render"]?.toBoolean() ?: false,
            )
        }
    }
}
