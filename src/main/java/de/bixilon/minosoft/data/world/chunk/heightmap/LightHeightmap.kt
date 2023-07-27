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

package de.bixilon.minosoft.data.world.chunk.heightmap

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight

class LightHeightmap(chunk: Chunk) : Heightmap(chunk) {


    override fun recalculate() {
        super.recalculate()
        chunk.light.sky.calculate()
    }

    override fun onHeightmapUpdate(x: Int, z: Int, previous: Int, now: Int) {
        if (previous > now) {
            // block is lower
            return chunk.light.sky.startFloodFill(x, z)
        }
        // block is now higher
        // ToDo: Neighbours
        val sections = chunk.sections
        val maxIndex = previous.sectionHeight - chunk.minSection
        val minIndex = now.sectionHeight - chunk.minSection
        chunk.light.bottom.reset()
        for (index in maxIndex downTo minIndex) {
            val section = sections[index] ?: continue
            section.light.reset()
        }
        for (index in maxIndex downTo minIndex) {
            val section = sections[index] ?: continue
            section.light.calculate()
        }
        chunk.light.sky.calculate()
    }


    override fun passes(state: BlockState): HeightmapPass {
        val light = state.block.getLightProperties(state)
        if (!light.skylightEnters) return HeightmapPass.ABOVE

        if (light.filtersSkylight) return HeightmapPass.IN
        if (!light.propagatesLight(Directions.DOWN)) return HeightmapPass.IN


        return HeightmapPass.PASSES
    }
}
