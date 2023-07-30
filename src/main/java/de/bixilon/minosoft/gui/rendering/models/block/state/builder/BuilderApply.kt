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

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.block.state.apply.BlockStateApply
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedFace
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.compact
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.compactProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture

class BuilderApply(
    val applies: List<BlockStateApply>
) : BlockStateApply {

    override fun bake(textures: TextureManager): BlockRender? {
        val static: MutableList<BakedModel> = mutableListOf()
        val dynamic: MutableList<BlockRender> = mutableListOf()

        for (apply in this.applies) {
            val baked = apply.bake(textures) ?: continue
            if (baked is BakedModel) {
                static += baked
            } else {
                dynamic += baked
            }
        }

        if (static.isEmpty() && dynamic.isEmpty()) return null

        val combined = static.combine()

        if (dynamic.isEmpty()) return combined


        return BuiltModel(combined, dynamic.toTypedArray())
    }


    private fun List<BakedModel>.combine(): BakedModel {
        val faces: Array<MutableList<BakedFace>> = Array(Directions.SIZE) { mutableListOf() }
        val properties: Array<MutableList<FaceProperties>> = Array(Directions.SIZE) { mutableListOf() }
        var particle: Texture? = null

        for (model in this) {
            if (particle == null) {
                particle = model.particle
            }

            for (direction in Directions) {
                faces[direction.ordinal] += model.faces[direction.ordinal]
                properties[direction.ordinal] += model.properties[direction.ordinal]?.faces ?: continue
            }
        }

            return BakedModel(faces.compact(), properties.compactProperties(), particle)
    }
}
