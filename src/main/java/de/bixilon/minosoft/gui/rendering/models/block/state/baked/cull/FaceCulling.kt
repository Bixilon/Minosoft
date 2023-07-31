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

package de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedFace
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies

object FaceCulling {

    inline fun canCull(state: BlockState, face: BakedFace, direction: Directions, neighbour: BlockState?): Boolean {
        return canCull(state, face.properties, direction, neighbour)
    }

    fun canCull(state: BlockState, properties: FaceProperties?, direction: Directions, neighbour: BlockState?): Boolean {
        if (neighbour == null) return false
        if (properties == null) return false

        val model = neighbour.model ?: return false
        val neighbourProperties = model.getProperties(direction) ?: return false // not touching side


        if (!properties.isCoveredBy(neighbourProperties)) return false

        if (neighbourProperties.transparency == TextureTransparencies.OPAQUE) {
            // impossible to see that face
            return true
        }

        if (state.block is CustomBlockCulling) {
            return state.block.shouldCull(state, properties, direction, neighbour)
        }

        if (neighbourProperties.transparency == null) return false // can not determinate it

        if (state.block != neighbour.block) return false
        if (neighbourProperties.transparency == properties.transparency) return true

        return false
    }


    private inline val BakedFace.touchingSide: Boolean get() = properties != null


    // TODO: merge with DirectedProperty
    private fun SideProperties.getSideArea(target: FaceProperties): Float {
        // overlapping is broken, see https://stackoverflow.com/questions/7342935/algorithm-to-compute-total-area-covered-by-a-set-of-overlapping-segments
        var area = 0.0f

        for (quad in this.faces) {
            val width = minOf(target.end.x, quad.end.x) - maxOf(quad.start.x, target.start.x)
            val height = minOf(target.end.y, quad.end.y) - maxOf(quad.start.y, target.start.y)

            area += width * height
        }

        return area
    }

    fun FaceProperties.isCoveredBy(properties: SideProperties): Boolean {
        val area = properties.getSideArea(this)
        return surface <= area
    }
}
