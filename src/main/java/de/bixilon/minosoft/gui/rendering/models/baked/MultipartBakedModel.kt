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

package de.bixilon.minosoft.gui.rendering.models.baked

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel
import de.bixilon.minosoft.gui.rendering.models.properties.AbstractFaceProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import java.util.*

class MultipartBakedModel(
    val models: Array<BakedBlockModel>,
    val sizes: Array<Array<AbstractFaceProperties>?>,
    val particleTexture: Texture?,
) : BakedBlockModel {

    override fun getTouchingFaceProperties(random: Random, direction: Directions): Array<AbstractFaceProperties>? {
        return sizes[direction.ordinal]
    }

    override fun singleRender(position: Vec3i, offset: FloatArray, mesh: ChunkMesh, random: Random, blockState: BlockState, neighbours: Array<BlockState?>, light: ByteArray, tints: IntArray?): Boolean {
        var rendered = false
        for (model in models) {
            if (model.singleRender(position, offset, mesh, random, blockState, neighbours, light, tints) && !rendered) {
                rendered = true
            }
        }
        return rendered
    }

    override fun getParticleTexture(random: Random, blockPosition: Vec3i): Texture? {
        return particleTexture
    }
}
