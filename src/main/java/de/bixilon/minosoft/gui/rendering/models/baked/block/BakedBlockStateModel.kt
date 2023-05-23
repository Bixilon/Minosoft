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

package de.bixilon.minosoft.gui.rendering.models.baked.block

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.array.IntArrayUtil.getOrElse
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetBlock
import de.bixilon.minosoft.data.world.container.block.SectionOcclusion.Companion.isFullyOpaque
import de.bixilon.minosoft.data.world.positions.BlockPositionUtil
import de.bixilon.minosoft.gui.rendering.models.CullUtil.canCull
import de.bixilon.minosoft.gui.rendering.models.properties.AbstractFaceProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.tint.TintManager
import de.bixilon.minosoft.gui.rendering.util.VecUtil.getWorldOffset
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.toVec3
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.preparer.cull.SolidCullSectionPreparer
import java.util.*

class BakedBlockStateModel(
    val faces: Array<Array<BakedFace>>,
    val touchingFaceProperties: Array<Array<AbstractFaceProperties>?>,
    val particleTexture: AbstractTexture?,
) : BakedBlockModel {

    override fun getTouchingFaceProperties(random: Random, direction: Directions): Array<AbstractFaceProperties>? {
        return touchingFaceProperties[direction.ordinal]
    }

    override fun singleRender(position: Vec3i, offset: FloatArray, mesh: WorldMesh, random: Random, blockState: BlockState, neighbours: Array<BlockState?>, light: ByteArray, tints: IntArray?): Boolean {
        val floatPosition = offset.toVec3()
        if (blockState.block is RandomOffsetBlock) {
            blockState.block.randomOffset?.let { floatPosition += position.getWorldOffset(it) }
        }
        val positionArray = floatPosition.array
        var rendered = false
        var tint: Int
        var currentLight: Int
        for ((index, faces) in faces.withIndex()) {
            val direction = Directions.VALUES[index]
            val neighbour = neighbours[index]
            val neighbourFullyOpaque = neighbour.isFullyOpaque()
            if (blockState.isFullyOpaque() && neighbourFullyOpaque) {
                continue
            }
            val neighboursModel = neighbour?.blockModel
            var neighbourProperties: Array<AbstractFaceProperties>? = null
            if (neighboursModel != null) {
                random.setSeed(BlockPositionUtil.generatePositionHash(position.x + direction.vector.x, position.y + direction.vector.y, position.z + direction.vector.z))
                neighbourProperties = neighboursModel.getTouchingFaceProperties(random, direction.inverted)
            }
            for (face in faces) {
                if (face.touching) {
                    if (neighbourFullyOpaque) {
                        continue
                    }
                    if (neighbourProperties != null && neighbourProperties.isNotEmpty() && neighbourProperties.canCull(face, neighbour != null && blockState.block.canCull(blockState, neighbour))) {
                        continue
                    }
                }
                tint = tints?.getOrElse(face.tintIndex, TintManager.DEFAULT_TINT_INDEX) ?: TintManager.DEFAULT_TINT_INDEX
                currentLight = (face.cullFace?.let { light[it.ordinal] } ?: light[SolidCullSectionPreparer.SELF_LIGHT_INDEX]).toInt()
                face.singleRender(positionArray, mesh, currentLight, tint)
                if (!rendered) {
                    rendered = true
                }
            }
        }
        return rendered
    }

    override fun getParticleTexture(random: Random, blockPosition: Vec3i): AbstractTexture? {
        return particleTexture
    }
}
