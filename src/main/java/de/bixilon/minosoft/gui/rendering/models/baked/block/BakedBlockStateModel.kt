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
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMesh
import de.bixilon.minosoft.gui.rendering.models.FaceSize
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.toVec3
import glm_.vec3.Vec3i
import java.util.*

class BakedBlockStateModel(
    val faces: Array<Array<BakedFace>>,
) : BakedBlockModel, GreedyBakedBlockModel { // ToDo: Greedy meshable
    override val canGreedyMesh: Boolean = true

    override fun getFaceSize(direction: Directions, random: Random): Array<FaceSize> {
        return arrayOf() // ToDo
    }

    override fun singleRender(position: Vec3i, mesh: ChunkSectionMesh, random: Random, neighbours: Array<BlockState?>, light: Int, ambientLight: IntArray) {
        val floatPosition = position.toVec3()
        for ((index, direction) in faces.withIndex()) {
            val neighbour = neighbours[index]
            if (neighbour != null) {
                continue
            }
            for (face in direction) {
                face.singleRender(floatPosition, mesh, neighbour, light, ambientLight)
            }
        }
    }

    override fun greedyRender(start: Vec3i, end: Vec3i, side: Directions, mesh: ChunkSectionMesh, light: Int) {
        val floatStart = start.toVec3()
        val floatEnd = end.toVec3()
        for (face in faces[side.ordinal]) {
            face.greedyRender(floatStart, floatEnd, side, mesh, light)
        }
    }

    override fun getLight(position: Vec3i, random: Random, side: Directions, lightAccessor: LightAccessor): Int {
        TODO("Not yet implemented")
    }
}
