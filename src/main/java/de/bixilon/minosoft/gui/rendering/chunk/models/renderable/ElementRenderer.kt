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

package de.bixilon.minosoft.gui.rendering.chunk.models.renderable

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelElement
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelFace
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import glm_.Java.Companion.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class ElementRenderer(element: BlockModelElement, rotation: Vec3, uvlock: Boolean, rescale: Boolean) {
    private val fullFaceDirections: MutableSet<Directions> = mutableSetOf()
    private val faces: MutableMap<Directions, BlockModelFace> = HashMap(element.faces)
    private var positions: Array<Vec3> = element.positions.clone()
    private val directionMapping: MutableMap<Directions, Directions> = mutableMapOf()

    init {
        rotatePositionsAxes(positions, rotation, rescale)
        // TODO : uvlock
        for (direction in Directions.DIRECTIONS) {
            if (positions.containsAllVectors(BlockModelElement.fullTestPositions[direction], 0.0001f)) { // TODO: check if texture is transparent ==> && ! texture.isTransparent
                fullFaceDirections.add(direction)
            }
            directionMapping[direction] = getRotatedDirection(rotation, direction)
            for (face in faces.values) {
                face.rotate(rotation.y.toDouble())
            }
        }
    }


    fun render(tintColor: RGBColor?, textureMapping: MutableMap<String, Texture>, modelMatrix: Mat4, direction: Directions, data: MutableList<Float>) {
        val realDirection = directionMapping[direction]!!
        val positionTemplate = BlockModelElement.FACE_POSITION_MAP_TEMPLATE[realDirection.ordinal]

        val face = faces[realDirection] ?: return // Not our face
        val texture = textureMapping[face.textureName] ?: TextureArray.DEBUG_TEXTURE
        // if (texture.isTransparent) {
        //     return // ToDo: force render transparent faces
        // }

        val drawPositions = arrayOf(positions[positionTemplate[0]], positions[positionTemplate[1]], positions[positionTemplate[2]], positions[positionTemplate[3]])

        fun addToData(vec3: Vec3, textureCoordinates: Vec2) {
            val input = Vec4(vec3, 1.0f)
            val output = modelMatrix * input
            data.add(output.x)
            data.add(output.y)
            data.add(output.z)
            data.add(textureCoordinates.x * texture.widthFactor)
            data.add(textureCoordinates.y * texture.heightFactor)
            data.add(Float.fromBits(texture.id)) // ToDo: Compact this

            // ToDo: Send this only once per texture
            data.add(texture.animationFrameTime.toFloat())
            data.add(texture.animations.toFloat())
            data.add(texture.heightFactor)

            if (face.tint && tintColor != null) {
                data.add(Float.fromBits(tintColor.color))
            } else {
                data.add(0f)
            }
        }

        fun createQuad(drawPositions: Array<Vec3>, texturePositions: Array<Vec2?>) {
            for (vertex in drawOrder) {
                addToData(drawPositions[vertex.first], texturePositions[vertex.second]!!)
            }
        }

        val texturePositions = face.getTexturePositionArray(realDirection)
        createQuad(drawPositions, texturePositions)
    }

    fun getTexture(direction: Directions): String? {
        return faces[direction]?.textureName
    }

    fun isCullFace(direction: Directions): Boolean {
        return faces[direction]?.cullFace == direction
    }

    fun isFullTowards(direction: Directions): Boolean {
        return fullFaceDirections.contains(direction)
    }

    companion object {
        fun createElements(state: JsonObject, parent: BlockModel): MutableList<ElementRenderer> {
            val rotation = glm.radians(vec3InJsonObject(state))
            val uvlock = state["uvlock"]?.asBoolean ?: false
            val rescale = state["rescale"]?.asBoolean ?: false
            val parentElements = parent.elements
            val result: MutableList<ElementRenderer> = mutableListOf()
            for (parentElement in parentElements) {
                result.add(ElementRenderer(parentElement, rotation, true, rescale)) // uvlock is not yet implemented in the data generator
            }
            return result
        }

        private fun vec3InJsonObject(json: JsonObject): Vec3 {
            return Vec3(json["x"]?.asFloat ?: 0, json["y"]?.asFloat ?: 0, json["z"]?.asFloat ?: 0)
        }

        val drawOrder = arrayOf(
            Pair(0, 1),
            Pair(3, 2),
            Pair(2, 3),
            Pair(2, 3),
            Pair(1, 0),
            Pair(0, 1),
        )

        private fun Array<Vec3>.containsAllVectors(set: Set<Vec3>?, margin: Float): Boolean {
            for (position in set!!) {
                var isIn = false
                for (testposition in this) {
                    if ((position - testposition).length() < margin) {
                        isIn = true
                    }
                }
                if (!isIn) {
                    return false
                }
            }
            return true
        }

        fun getRotatedDirection(rotation: Vec3, direction: Directions): Directions {
            if (rotation == Vec3(0, 0, 0)) {
                return direction
            }
            var rotatedDirectionVector = BlockModelElement.rotateVector(direction.directionVector, rotation.z.toDouble(), Axes.Z)
            rotatedDirectionVector = BlockModelElement.rotateVector(rotatedDirectionVector, rotation.y.toDouble(), Axes.Y)
            return Directions.byDirection(BlockModelElement.rotateVector(rotatedDirectionVector, rotation.x.toDouble(), Axes.X))
        }

        fun rotatePositionsAxes(positions: Array<Vec3>, angles: Vec3, rescale: Boolean) {
            if (angles == Vec3()) {
                return
            }
            BlockModelElement.rotatePositions(positions, Axes.X, angles.x.toDouble(), Vec3(), rescale)
            BlockModelElement.rotatePositions(positions, Axes.Y, angles.y.toDouble(), Vec3(), rescale)
            BlockModelElement.rotatePositions(positions, Axes.Z, angles.z.toDouble(), Vec3(), rescale)
        }
    }
}
