/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.block.renderable.block

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionArrayMesh
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMeshCollection
import de.bixilon.minosoft.gui.rendering.block.models.BlockModel
import de.bixilon.minosoft.gui.rendering.block.models.BlockModelElement
import de.bixilon.minosoft.gui.rendering.block.models.BlockModelFace
import de.bixilon.minosoft.gui.rendering.block.models.FaceSize
import de.bixilon.minosoft.gui.rendering.block.renderable.BlockLikeRenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.rotate
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.util.KUtil.toBoolean
import glm_.vec3.Vec3

class ElementRenderer(
    val model: BlockModel,
    val element: BlockModelElement,
    val rotation: Vec3,
    data: Map<String, Any>,
    private val directionMapping: HashBiMap<Directions, Directions>,
) {
    val faceBorderSize: Array<FaceSize?> = arrayOfNulls(Directions.VALUES.size)
    private val faces: Map<Directions, BlockModelFace>
    private var transformedPositions: Array<Vec3> = element.transformedPositions.clone()

    init {
        rotatePositionsAxes(transformedPositions, rotation, data["rescale"]?.toBoolean() ?: model.rescale)

        val faces: MutableMap<Directions, BlockModelFace> = mutableMapOf()
        for (direction in Directions.VALUES) {

            direction.getFaceBorderSizes(element.from, element.to)?.let {
                faceBorderSize[direction.ordinal] = it
            }

            element.faces[direction]?.let {
                faces[direction] = BlockModelFace(it)
            }
        }
        if (data["uvlock"]?.toBoolean() ?: model.uvLock) {
            for (direction in Directions.VALUES) {
                val axis = Axes[direction]
                val angle = axis.choose(rotation) * axis.choose(direction.vector)
                faces[direction] = faces[direction]?.rotate(-angle) ?: continue
            }
        }
        this.faces = faces.toMap()
    }

    fun render(tintColor: RGBColor?, textureMapping: MutableMap<String, AbstractTexture>, direction: Directions, context: BlockLikeRenderContext) {
        val realDirection = directionMapping.inverse()[direction]!!

        val face = faces[realDirection] ?: return // Not our face
        val positionTemplate = BlockModelElement.FACE_POSITION_MAP_TEMPLATE[realDirection.ordinal]

        val texture = textureMapping[face.textureName] ?: TODO("Unknown texture used ${face.textureName}") // ToDo: can be replaced with RenderConstants.DEBUG_TEXTURE_ID?

        val shadeLevel = when {
            !element.shade -> 1.0f
            direction == Directions.UP -> 1.0f
            direction == Directions.DOWN -> 0.5f
            direction == Directions.EAST || direction == Directions.WEST -> 0.6f
            direction == Directions.NORTH || direction == Directions.SOUTH -> 0.8f
            else -> TODO()
        }

        var finalColor = if (face.tint && tintColor != null) {
            tintColor
        } else {
            ChatColors.WHITE
        }

        finalColor = finalColor.with(finalColor.floatRed * shadeLevel, finalColor.floatGreen * shadeLevel, finalColor.floatBlue * shadeLevel)

        val lightPosition = context.blockPosition + face.cullFace?.let { directionMapping[it] }// ToDo: rotate cullface
        val light = context.world.getLight(lightPosition)

        val drawPositions = mutableListOf<Vec3>()
        for (position in positionTemplate) {
            drawPositions += transformedPositions[position]
        }

        val mesh = getMesh(context.meshCollection, texture.transparency)
        val texturePositions = face.getTexturePositionArray(realDirection)

        for ((drawPositionIndex, texturePositionIndex) in Mesh.QUAD_DRAW_ODER) {
            val input = drawPositions[drawPositionIndex]
            val output = context.blockPosition.toVec3 + input + DRAW_OFFSET + context.offset

            mesh.addVertex(
                position = output,
                uv = texturePositions[texturePositionIndex]!!,
                texture = texture,
                tintColor = finalColor,
                light = light,
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

        fun createElements(data: Map<String, Any>, model: BlockModel, rotation: Vec3, directionMapping: HashBiMap<Directions, Directions>): List<ElementRenderer> {
            val result: MutableList<ElementRenderer> = mutableListOf()
            for (element in model.elements) {
                result += ElementRenderer(model, element, rotation, data, directionMapping)
            }
            return result.toList()
        }

        fun getRotatedDirection(rotation: Vec3, direction: Directions): Directions {
            if (rotation == Vec3.EMPTY) {
                return direction
            }

            return Directions.byDirection(
                direction.vectorf.rotate(-rotation.x, Axes.X)
                    .rotate(rotation.y, Axes.Y)
                    .rotate(-rotation.z, Axes.Z)
            )
        }

        fun rotatePositionsAxes(positions: Array<Vec3>, angles: Vec3, rescale: Boolean) {
            if (angles == Vec3.EMPTY) {
                return
            }
            BlockModelElement.rotatePositions(positions, Axes.X, -angles.x, Vec3.EMPTY, rescale)
            BlockModelElement.rotatePositions(positions, Axes.Y, angles.y, Vec3.EMPTY, rescale)
            BlockModelElement.rotatePositions(positions, Axes.Z, -angles.z, Vec3.EMPTY, rescale)
        }

        val POSITION_1 = Vec3(-0.5f, -0.5f, -0.5f)
        val POSITION_2 = Vec3(+0.5f, -0.5f, -0.5f)
        val POSITION_3 = Vec3(-0.5f, -0.5f, +0.5f)
        val POSITION_4 = Vec3(+0.5f, -0.5f, +0.5f)

        val DRAW_OFFSET = Vec3(+0.5f, +0.5f, +0.5f)

        fun getMesh(meshCollection: ChunkSectionMeshCollection, transparency: TextureTransparencies): ChunkSectionArrayMesh {
            return when (transparency) {
                TextureTransparencies.OPAQUE -> meshCollection.opaqueMesh
                TextureTransparencies.TRANSPARENT -> meshCollection.transparentMesh!!
                TextureTransparencies.TRANSLUCENT -> meshCollection.translucentMesh!!
            }
        }
    }
}
