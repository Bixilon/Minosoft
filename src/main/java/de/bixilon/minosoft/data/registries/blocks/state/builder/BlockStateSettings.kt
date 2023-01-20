/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.shapes.ShapeRegistry
import de.bixilon.minosoft.data.registries.shapes.VoxelShape

class BlockStateSettings(
    val properties: Map<BlockProperties, Any>?,
    val luminance: Int,
    val collisionShape: VoxelShape?,
    val outlineShape: VoxelShape?,
    val lightProperties: LightProperties,
) {

    companion object {

        private fun Any.getShape(shapes: ShapeRegistry): VoxelShape {
            if (this is Int) {
                return shapes[this]
            }
            return VoxelShape(shapes, this)
        }

        private fun JsonObject.getCollisionShape(shapes: ShapeRegistry): VoxelShape {
            this["collision_shape"]?.getShape(shapes)?.let { return it }

            this["is_collision_shape_full_block"]?.toBoolean()?.let { if (it) return VoxelShape.FULL else VoxelShape.EMPTY }

            return VoxelShape.EMPTY
        }

        private fun JsonObject.getOutlineShape(shapes: ShapeRegistry): VoxelShape {
            this["outline_shape"]?.getShape(shapes)?.let { return it }

            return VoxelShape.EMPTY
        }

        private fun JsonObject.getProperties(): Map<BlockProperties, Any>? {
            val data = this["properties"]?.toJsonObject() ?: return null
            if (data.isEmpty()) return null

            val properties: MutableMap<BlockProperties, Any> = mutableMapOf()

            for ((group, json) in data) {
                try {
                    val (property, value) = BlockProperties.parseProperty(group, if (json is String) json.lowercase() else json)
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

        private fun JsonObject.getLightProperties(outlineShape: VoxelShape): LightProperties {
            val opaque = this["is_opaque"]?.toBoolean() ?: true
            val translucent = this["translucent"]?.toBoolean() ?: true


            // TODO: FluidBlock

            var lightProperties = if (outlineShape == VoxelShape.EMPTY || (!opaque && translucent)) {
                TransparentProperty
            } else if (outlineShape == VoxelShape.FULL) {
                OpaqueProperty
            } else {
                DirectedProperty.of(outlineShape, opaque, !translucent)
            }


            if (lightProperties is OpaqueProperty && !opaque) {
                lightProperties = CustomLightProperties(propagatesLight = true, skylightEnters = false, !translucent)
            }
            return lightProperties
        }

        fun of(registries: Registries, data: JsonObject): BlockStateSettings {
            val collisionShape = data.getCollisionShape(registries.shape)
            val outlineShape = data.getOutlineShape(registries.shape)


            return BlockStateSettings(
                properties = data.getProperties(),
                luminance = data.getLuminance(),
                collisionShape = if (collisionShape == VoxelShape.EMPTY) null else collisionShape,
                outlineShape = if (outlineShape == VoxelShape.EMPTY) null else outlineShape,
                lightProperties = data.getLightProperties(outlineShape),
            )
        }
    }
}
