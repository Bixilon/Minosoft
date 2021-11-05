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

package de.bixilon.minosoft.gui.rendering.models.baked

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.gui.rendering.block.mesh.ChunkSectionMesh
import de.bixilon.minosoft.gui.rendering.models.FaceSize
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel
import glm_.vec3.Vec3i
import java.util.*
import kotlin.math.abs

class WeightedBakedModel(
    val models: Map<BakedBlockModel, Int>,
) : BakedBlockModel {
    val totalWeight: Int

    init {
        var totalWeight = 0
        for ((_, weight) in models) {
            totalWeight += weight
        }

        this.totalWeight = totalWeight
    }

    private fun getModel(random: Random): BakedBlockModel {
        val totalWeight = abs(random.nextLong() % totalWeight)

        var weightLeft = totalWeight

        for ((model, weight) in models) {
            weightLeft -= weight
            if (weightLeft < 0) {
                return model
            }
        }

        throw IllegalStateException("Could not find a model: This should never happen!")
    }

    override fun getFaceSize(direction: Directions, random: Random): Array<FaceSize> {
        return getModel(random).getFaceSize(direction, random)
    }

    override fun getLight(position: Vec3i, random: Random, side: Directions, lightAccessor: LightAccessor): Int {
        return getModel(random).getLight(position, random, side, lightAccessor)
    }

    override fun singleRender(position: Vec3i, mesh: ChunkSectionMesh, random: Random, neighbours: Array<BlockState?>, light: Int, ambientLight: IntArray) {
        getModel(random).singleRender(position, mesh, random, neighbours, light, ambientLight)
    }

}
