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

package de.bixilon.minosoft.gui.rendering.models.block.state.render

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.BlockPositionUtil.positionHash
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.SideSize
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import java.util.*
import kotlin.math.abs

class WeightedBlockRender(
    val models: Array<WeightedEntry>,
    val totalWeight: Int,
) : BlockRender {
    private val size = models.getSize()

    override fun getSize(direction: Directions): SideSize? {
        return size[direction.ordinal] // TODO: get random block model
    }

    private fun getModel(random: Random?, position: BlockPosition): BlockRender {
        if (random == null) return models.first().model
        random.setSeed(position.positionHash)

        var weightLeft = abs(random.nextLong() % totalWeight)

        for ((weight, model) in models) {
            weightLeft -= weight
            if (weightLeft >= 0) continue

            return model
        }

        Broken("Could not find a model: This should never happen!")
    }

    override fun getParticleTexture(random: Random?, position: Vec3i): AbstractTexture? {
        return getModel(random, position).getParticleTexture(random, position)
    }

    override fun render(position: BlockPosition, offset: FloatArray, mesh: WorldMesh, random: Random?, state: BlockState, neighbours: Array<BlockState?>, light: ByteArray, tints: IntArray?): Boolean {
        return getModel(random, position).render(position, offset, mesh, random, state, neighbours, light, tints)
    }


    data class WeightedEntry(
        val weight: Int,
        val model: BakedModel,
    )

    private fun Array<WeightedEntry>.getSize(): Array<SideSize?> {
        val sizes: Array<SideSize?> = arrayOfNulls(Directions.SIZE)
        val skip = BooleanArray(Directions.SIZE)

        for ((_, model) in this) {
            for (direction in Directions) {
                if (skip[direction.ordinal]) continue

                val current = sizes[direction.ordinal]
                val size = model.getSize(direction)
                if (current == null) {
                    sizes[direction.ordinal] = size
                    continue
                }
                if (current != size) {
                    skip[direction.ordinal] = true
                    continue
                }
            }
        }

        for ((index, skip) in skip.withIndex()) {
            if (!skip) continue
            sizes[index] = null
        }

        return sizes
    }
}
