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
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies

object FaceCulling {

    fun canCull(state: BlockState, properties: FaceProperties?, direction: Directions, neighbour: BlockState?): Boolean {
        if (neighbour == null) return false
        if (properties == null) return false

        val model = neighbour.model ?: neighbour.block.model ?: return false
        val neighbourProperties = model.getProperties(direction) ?: return false // not touching side


        if (!properties.isCoveredBy(neighbourProperties)) return false

        if (neighbourProperties.transparency == TextureTransparencies.OPAQUE) {
            // impossible to see that face
            return true
        }
        if (neighbourProperties.transparency == null) {
            for (property in neighbourProperties.faces) {
                if (property.transparency != TextureTransparencies.OPAQUE) continue
                if (!properties.isCoveredBy(property)) continue
                return true
            }
        }

        if (state.block is CustomBlockCulling) {
            return state.block.shouldCull(state, properties, direction, neighbour)
        }

        if (neighbourProperties.transparency == null) {
            return false
        }

        if (state.block != neighbour.block) return false
        if (neighbourProperties.transparency == properties.transparency) return true

        return false
    }

    // TODO: merge with DirectedProperty
    private fun SideProperties.getSideArea(target: FaceProperties): Float {
        // overlapping is broken, see https://stackoverflow.com/questions/7342935/algorithm-to-compute-total-area-covered-by-a-set-of-overlapping-segments
        var area = 0.0f

        for (quad in this.faces) {
            area += quad.getSideArea(target)
        }

        return area
    }

    private inline fun minOf(a: Float, b: Float): Float {
        if (a < b) return a
        return b
    }

    private inline fun maxOf(a: Float, b: Float): Float {
        if (a > b) return a
        return b
    }

    private fun FaceProperties.getSideArea(target: FaceProperties): Float {
        val start = start.array
        val targetStart = target.start.array
        val end = end.array
        val targetEnd = target.end.array

        val width = minOf(targetEnd[0], end[0]) - maxOf(start[0], targetStart[0])
        val height = minOf(targetEnd[1], end[1]) - maxOf(start[1], targetStart[1])

        return width * height
    }

    fun FaceProperties.isCoveredBy(properties: SideProperties): Boolean {
        val area = properties.getSideArea(this)
        return surface <= area
    }

    fun FaceProperties.isCoveredBy(properties: FaceProperties): Boolean {
        val area = properties.getSideArea(this)
        return surface <= area
    }
}
