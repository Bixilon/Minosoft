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

package de.bixilon.minosoft.gui.rendering.models.baked.block

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMesh
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMeshes
import de.bixilon.minosoft.gui.rendering.models.CullUtil.canCull
import de.bixilon.minosoft.gui.rendering.models.FaceProperties
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getWorldOffset
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.toVec3
import glm_.vec3.Vec3i
import java.util.*

class BakedBlockStateModel(
    private val faces: Array<Array<BakedFace>>,
    private val touchingFaceProperties: Array<Array<FaceProperties>>,
) : BakedBlockModel, GreedyBakedBlockModel { // ToDo: Greedy meshable
    override val canGreedyMesh: Boolean = true
    override val greedyMeshableFaces: BooleanArray = booleanArrayOf(true, false, true, true, true, true)

    override fun getTouchingFaceProperties(random: Random, direction: Directions): Array<FaceProperties> {
        return touchingFaceProperties[direction.ordinal]
    }

    override fun singleRender(position: Vec3i, mesh: ChunkSectionMeshes, random: Random, blockState: BlockState, neighbours: Array<BlockState?>, light: Int, ambientLight: FloatArray, tints: IntArray?): Boolean {
        val floatPosition = position.toVec3()
        blockState.block.randomOffsetType?.let {
            floatPosition += position.getWorldOffset(blockState.block)
        }
        val positionArray = floatPosition.array
        var rendered = false
        var tint: Int
        for ((index, faces) in faces.withIndex()) {
            val direction = Directions.VALUES[index]
            val neighbour = neighbours[index]
            val neighboursModel = neighbour?.model
            var neighbourProperties: Array<FaceProperties>? = null
            if (neighboursModel != null) {
                random.setSeed(VecUtil.generatePositionHash(position.x + direction.vector.x, position.y + direction.vector.y, position.z + direction.vector.z))
                neighbourProperties = neighboursModel.getTouchingFaceProperties(random, direction.inverted)
            }
            for (face in faces) {
                if (face.touching && neighbourProperties != null && neighbourProperties.isNotEmpty() && neighbourProperties.canCull(face, neighbour != null && blockState.block.canCull(blockState, neighbour))) {
                    continue
                }
                tint = tints?.getOrNull(face.tintIndex) ?: -1
                face.singleRender(positionArray, mesh, light, ambientLight, tint)
                if (!rendered) {
                    rendered = true
                }
            }
        }
        return rendered
    }

    override fun greedyRender(start: Vec3i, end: Vec3i, side: Directions, mesh: ChunkSectionMesh, light: Int) {
        val floatStart = start.toVec3()
        val floatEnd = end.toVec3()
        for (face in faces[side.ordinal]) {
            face.greedyRender(floatStart, floatEnd, side, mesh, light)
        }
    }
}
