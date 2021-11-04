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
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMesh
import de.bixilon.minosoft.gui.rendering.models.FaceSize
import glm_.vec3.Vec3i
import java.util.*

class BakedBlockStateModel(
    val faces: Array<Array<BakedFace>>,
    val sizes: Array<Array<FaceSize>>,
) : BakedBlockModel {

    override fun getFaceSize(direction: Directions, random: Random): Array<FaceSize> {
        return sizes[direction.ordinal]
    }

    override fun singleRender(position: Vec3i, mesh: ChunkSectionMesh, random: Random, light: Int, ambientLight: IntArray) {
        for (direction in faces) {
            for (face in direction) {
                face.singleRender(position, mesh, light, ambientLight)
            }
        }
    }

    override fun getLight(position: Vec3i, random: Random, side: Directions, lightAccessor: LightAccessor): Int {
        TODO("Not yet implemented")
    }
}
