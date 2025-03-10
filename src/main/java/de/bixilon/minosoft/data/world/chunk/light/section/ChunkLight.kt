/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.chunk.light.section

import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.heightmap.FixedHeightmap
import de.bixilon.minosoft.data.world.chunk.heightmap.LightHeightmap
import de.bixilon.minosoft.data.world.chunk.light.section.ChunkLightUtil.hasSkyLight
import de.bixilon.minosoft.data.world.chunk.light.section.border.BottomSectionLight
import de.bixilon.minosoft.data.world.chunk.light.section.border.TopSectionLight
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.positions.InChunkPosition

class ChunkLight(val chunk: Chunk) {
    val heightmap = if (chunk.world.dimension.hasSkyLight()) LightHeightmap(chunk) else FixedHeightmap.MAX_VALUE

    val bottom = BottomSectionLight(chunk)
    val top = TopSectionLight(chunk)

    val sky = ChunkSkyLight(this)


    fun onBlockChange(position: InChunkPosition, section: ChunkSection, previous: BlockState?, next: BlockState?) {
        heightmap.onBlockChange(position, next)

        section.light.onBlockChange(position.inSectionPosition, previous, next)
    }


    operator fun get(position: InChunkPosition): LightLevel {
        val sectionHeight = position.sectionHeight
        val inSection = position.inSectionPosition

        val light = when (sectionHeight) {
            chunk.minSection - 1 -> bottom[inSection]
            chunk.maxSection + 1 -> top[inSection]
            else -> chunk[sectionHeight]?.light?.get(inSection) ?: LightLevel.EMPTY
        }

        if (position.y >= heightmap[position]) {
            // set sky=15
            return light.with(sky = LightLevel.MAX_LEVEL)
        }
        return light
    }

    fun recalculate()
    fun calculate()
    fun reset()
    fun propagate()
}
