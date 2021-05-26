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

package de.bixilon.minosoft.gui.rendering.chunk.models.renderable

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.chunk.models.FaceSize
import de.bixilon.minosoft.gui.rendering.textures.Texture

class MultipartRenderer(
    val models: List<BlockLikeRenderer>,
) : BlockLikeRenderer {
    override val faceBorderSizes: Array<Array<FaceSize>?>
    override val transparentFaces: BooleanArray = BooleanArray(Directions.VALUES.size)

    init {
        val faceBorderSizes: MutableList<Array<FaceSize>?> = mutableListOf()

        for (model in models) {
            for (size in model.faceBorderSizes) {
                faceBorderSizes.add(size)
            }
            for ((index, direction) in model.transparentFaces.withIndex()) {
                if (direction) {
                    transparentFaces[index] = true
                }
            }
        }
        this.faceBorderSizes = faceBorderSizes.toTypedArray()
    }

    override fun render(context: BlockLikeRenderContext) {
        for (model in models) {
            model.render(context)
        }
    }

    override fun resolveTextures(textures: MutableMap<ResourceLocation, Texture>) {
        for (model in models) {
            model.resolveTextures(textures)
        }
    }
}
