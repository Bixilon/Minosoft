/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.models.renderable

import com.google.common.collect.HashBiMap
import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.chunk.ChunkMeshCollection
import de.bixilon.minosoft.gui.rendering.chunk.SectionArrayMesh
import de.bixilon.minosoft.gui.rendering.chunk.models.FaceSize
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModel
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelElement
import de.bixilon.minosoft.gui.rendering.chunk.models.loading.BlockModelFace
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.rotate
import glm_.vec3.Vec3

class ElementRenderer(
    parent: BlockModelElement,
    val rotation: Vec3,
    uvLock: Boolean,
    rescale: Boolean,
    private val directionMapping: HashBiMap<Directions, Directions>,
) {
    val faceBorderSize: Array<FaceSize?> = arrayOfNulls(Directions.VALUES.size)
    private val faces: MutableMap<Directions, BlockModelFace> = mutableMapOf()
    private var transformedPositions: Array<Vec3> = parent.transformedPositions.clone()
    private val from = parent.from
    private val to = parent.to

    init {
        rotatePositionsAxes(transformedPositions, rotation, rescale)
        for (direction in Directions.VALUES) {
            direction.getFaceBorderSizes(from, to)?.let {
                faceBorderSize[direction.ordinal] = it
            }
            parent.faces[direction]?.let {
                faces[direction] = BlockModelFace(it)
            }
        }
        if (uvLock) {
            for (direction in Directions.VALUES) {
                val axis = Axes.byDirection(direction)
                val angle = Axes.choose(axis, rotation) * Axes.choose(axis, direction.directionVector)
                faces[direction]?.rotate(-angle)
            }
        }
    }

    fun render(tintColor: RGBColor?, textureMapping: MutableMap<String, Texture>, direction: Directions, context: BlockLikeRenderContext) {
        val realDirection = directionMapping.inverse()[direction]!!

        val face = faces[realDirection] ?: return // Not our face
        val positionTemplate = BlockModelElement.FACE_POSITION_MAP_TEMPLATE[realDirection.ordinal]

        val texture = textureMapping[face.textureName] ?: TODO("Unknown texture used ${face.textureName}") // ToDo: can be replaced with RenderConstants.DEBUG_TEXTURE_ID?

        val lightLevel = context.lightAccessor.getLightLevel(context.blockPosition + face.cullFace?.let { directionMapping[it] }) // ToDo: rotate cullface

        val drawPositions = mutableListOf<Vec3>()
        for (position in positionTemplate) {
            drawPositions += transformedPositions[position]
        }

        val mesh = getMesh(context.meshCollection, texture.transparency)
        val texturePositions = face.getTexturePositionArray(realDirection)

        for ((drawPositionIndex, texturePositionIndex) in DRAW_ODER) {
            val input = drawPositions[drawPositionIndex]
            val output = context.blockPosition plus context.offset + input + DRAW_OFFSET
            mesh.addVertex(
                position = output,
                textureCoordinates = texturePositions[texturePositionIndex]!!,
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

    fun getTexture(direction: Directions): String? {
        return faces[direction]?.textureName
    }

    fun isCullFace(direction: Directions): Boolean {
        return faces[direction]?.cullFace == direction
    }

    companion object {
        val DRAW_ODER = arrayOf(
            0 to 1,
            3 to 2,
            2 to 3,
            2 to 3,
            1 to 0,
            0 to 1,
        )

        fun createElements(state: JsonObject, parent: BlockModel, rotation: Vec3, directionMapping: HashBiMap<Directions, Directions>): MutableList<ElementRenderer> {
            val uvLock = state["uvlock"]?.asBoolean ?: false
            val rescale = state["rescale"]?.asBoolean ?: false
            val parentElements = parent.elements
            val result: MutableList<ElementRenderer> = mutableListOf()
            for (parentElement in parentElements) {
                result.add(ElementRenderer(parentElement, rotation, uvLock, rescale, directionMapping))
            }
            return result
        }

        fun getRotatedDirection(rotation: Vec3, direction: Directions): Directions {
            if (rotation == VecUtil.EMPTY_VEC3) {
                return direction
            }
            var rotatedDirectionVector = direction.floatDirectionVector.rotate(-rotation.x, Axes.X)
            rotatedDirectionVector = rotatedDirectionVector.rotate(rotation.y, Axes.Y)
            return Directions.byDirection(rotatedDirectionVector.rotate(-rotation.z, Axes.Z))
        }

        fun rotatePositionsAxes(positions: Array<Vec3>, angles: Vec3, rescale: Boolean) {
            if (angles == VecUtil.EMPTY_VEC3) {
                return
            }
            BlockModelElement.rotatePositions(positions, Axes.X, -angles.x, VecUtil.EMPTY_VEC3, rescale)
            BlockModelElement.rotatePositions(positions, Axes.Y, angles.y, VecUtil.EMPTY_VEC3, rescale)
            BlockModelElement.rotatePositions(positions, Axes.Z, -angles.z, VecUtil.EMPTY_VEC3, rescale)
        }

        val POSITION_1 = Vec3(-0.5f, -0.5f, -0.5f)
        val POSITION_2 = Vec3(+0.5f, -0.5f, -0.5f)
        val POSITION_3 = Vec3(-0.5f, -0.5f, +0.5f)
        val POSITION_4 = Vec3(+0.5f, -0.5f, +0.5f)

        val DRAW_OFFSET = Vec3(+0.5f, +0.5f, +0.5f)

        fun getMesh(meshCollection: ChunkMeshCollection, textureTransparencies: TextureTransparencies): SectionArrayMesh {
            return if (textureTransparencies == TextureTransparencies.TRANSLUCENT) {
                meshCollection.transparentSectionArrayMesh!!
            } else {
                meshCollection.opaqueSectionArrayMesh
            }
        }
    }
}
