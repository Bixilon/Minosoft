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

package de.bixilon.minosoft.gui.rendering.models.block.state.builder

import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import java.util.*

class BuiltModel(
    // TODO: this fucks up the rendering order, because it mixes static models with dynamic ones
    val model: BakedModel,
    val dynamic: Array<BlockRender>,
) : BlockRender {

    override fun render(position: BlockPosition, offset: FloatArray, mesh: WorldMesh, random: Random?, state: BlockState, neighbours: Array<BlockState?>, light: ByteArray, tints: IntArray?): Boolean {
        var rendered = model.render(position, offset, mesh, random, state, neighbours, light, tints)

        for (dynamic in this.dynamic) {
            if (dynamic.render(position, offset, mesh, random, state, neighbours, light, tints)) {
                rendered = true
            }
        }

        return rendered
    }

    // TODO: getSize/culling support
}
