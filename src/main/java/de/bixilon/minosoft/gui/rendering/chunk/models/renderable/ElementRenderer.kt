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

import com.google.common.collect.HashBiMap
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.gui.rendering.chunk.ChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelElement
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelFace
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import glm_.Java.Companion.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class ElementRenderer(parent: BlockModelElement, val rotation: Vec3, uvLock: Boolean, rescale: Boolean) {
    private val fullFaceDirections: MutableSet<Directions> = mutableSetOf()
    private val faces: MutableMap<Directions, BlockModelFace> = mutableMapOf()
    private var positions: Array<Vec3> = parent.positions.clone()
    private val directionMapping: HashBiMap<Directions, Directions> = HashBiMap.create()

    init {
        rotatePositionsAxes(positions, rotation, rescale)
        // TODO : uvLock
        for (direction in Directions.DIRECTIONS) {
            if (positions.containsAllVectors(FULL_TEST_POSITIONS[direction.ordinal], 0.0001f)) { // TODO: check if texture is transparent ==> && ! texture.isTransparent
                fullFaceDirections.add(direction)
            }
            directionMapping[direction] = getRotatedDirection(rotation, direction)
            parent.faces[direction]?.let {
                faces[direction] = BlockModelFace(it)
            }
        }
    }


    fun render(tintColor: RGBColor?, position: BlockPosition, lightAccessor: LightAccessor, textureMapping: MutableMap<String, Texture>, modelMatrix: Mat4, direction: Directions, mesh: ChunkMesh) {
        val realDirection = directionMapping.inverse()[direction]!!

        val face = faces[realDirection] ?: return // Not our face

        val positionTemplate = BlockModelElement.FACE_POSITION_MAP_TEMPLATE[realDirection.ordinal]

        val texture = textureMapping[face.textureName] ?: TextureArray.DEBUG_TEXTURE

        val lightLevel = lightAccessor.getLightLevel(position + directionMapping[face.cullFace]) // ToDo: rotate cullface

        val drawPositions = arrayOf(positions[positionTemplate[0]], positions[positionTemplate[1]], positions[positionTemplate[2]], positions[positionTemplate[3]])

        fun createQuad(drawPositions: Array<Vec3>, texturePositions: Array<Vec2?>) {
            for (vertex in DRAW_ODER) {
                val input = Vec4(drawPositions[vertex.first], 1.0f)
                val output = modelMatrix * input
                mesh.addVertex(
                    position = output.toVec3(),
                    textureCoordinates = texturePositions[vertex.second]!!,
                    texture = texture,
                    tintColor = if (face.tint) {
                        tintColor
                    } else {
                        null
                    },
                    lightLevel = lightLevel,
                )
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
        private val EMPTY_VECTOR = Vec3()

        val DRAW_ODER = arrayOf(
            Pair(0, 1),
            Pair(3, 2),
            Pair(2, 3),
            Pair(2, 3),
            Pair(1, 0),
            Pair(0, 1),
        )

        fun createElements(state: JsonObject, parent: BlockModel): MutableList<ElementRenderer> {
            val rotation = glm.radians(state.asVec3())
            val uvLock = state["uvlock"]?.asBoolean ?: false
            val rescale = state["rescale"]?.asBoolean ?: false
            val parentElements = parent.elements
            val result: MutableList<ElementRenderer> = mutableListOf()
            for (parentElement in parentElements) {
                result.add(ElementRenderer(parentElement, rotation, uvLock, rescale))
            }
            return result
        }


        private fun Array<Vec3>.containsAllVectors(their: Set<Vec3>, margin: Float): Boolean {
            for (theirPosition in their) {
                var vec3WasIn = false
                for (thisPosition in this) {
                    if ((theirPosition - thisPosition).length() < margin) {
                        vec3WasIn = true
                        break
                    }
                }
                if (!vec3WasIn) {
                    return false
                }
            }
            return true
        }

        fun getRotatedDirection(rotation: Vec3, direction: Directions): Directions {
            if (rotation == EMPTY_VECTOR) {
                return direction
            }
            var rotatedDirectionVector = VecUtil.rotateVector(direction.directionVector, rotation.x.toDouble(), Axes.X)
            rotatedDirectionVector = VecUtil.rotateVector(rotatedDirectionVector, rotation.y.toDouble(), Axes.Y)
            return Directions.byDirection(VecUtil.rotateVector(rotatedDirectionVector, rotation.z.toDouble(), Axes.Z))
        }

        fun rotatePositionsAxes(positions: Array<Vec3>, angles: Vec3, rescale: Boolean) {
            if (angles == EMPTY_VECTOR) {
                return
            }
            BlockModelElement.rotatePositions(positions, Axes.X, angles.x.toDouble(), EMPTY_VECTOR, rescale)
            BlockModelElement.rotatePositions(positions, Axes.Y, angles.y.toDouble(), EMPTY_VECTOR, rescale)
            BlockModelElement.rotatePositions(positions, Axes.Z, angles.z.toDouble(), EMPTY_VECTOR, rescale)
        }

        private val POSITION_1 = Vec3(-0.5f, -0.5f, -0.5f)
        private val POSITION_2 = Vec3(+0.5f, -0.5f, -0.5f)
        private val POSITION_3 = Vec3(-0.5f, -0.5f, +0.5f)
        private val POSITION_4 = Vec3(+0.5f, -0.5f, +0.5f)
        private val POSITION_5 = Vec3(-0.5f, +0.5f, -0.5f)
        private val POSITION_6 = Vec3(+0.5f, +0.5f, +0.5f)
        private val POSITION_7 = Vec3(-0.5f, +0.5f, +0.5f)
        private val POSITION_8 = Vec3(+0.5f, +0.5f, +0.5f)

        val FULL_TEST_POSITIONS = arrayOf(
            setOf(POSITION_1, POSITION_2, POSITION_3, POSITION_4),
            setOf(POSITION_5, POSITION_6, POSITION_7, POSITION_8),
            setOf(POSITION_3, POSITION_4, POSITION_7, POSITION_8),
            setOf(POSITION_1, POSITION_2, POSITION_5, POSITION_6),
            setOf(POSITION_2, POSITION_4, POSITION_6, POSITION_8),
            setOf(POSITION_1, POSITION_3, POSITION_5, POSITION_7)
                                         )
    }
}

private fun JsonObject.asVec3(): Vec3 {
    return Vec3(this["x"]?.asFloat ?: 0, this["y"]?.asFloat ?: 0, this["z"]?.asFloat ?: 0)
}
