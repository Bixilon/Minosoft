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

package de.bixilon.minosoft.local.generator

import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkData
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight

class ChunkBuilder(
    val world: World,
    val position: ChunkPosition,
) {
    private val minSection = world.dimension.minSection
    private val blocks: Array<Array<BlockState?>?> = arrayOfNulls(world.dimension.sections)
    var biomes: BiomeSource? = null


    operator fun set(height: SectionHeight, data: Array<BlockState?>) {
        assert(data.size == ChunkSize.BLOCKS_PER_SECTION)
        blocks[height - minSection] = data
    }

    operator fun set(x: Int, y: Int, z: Int, state: BlockState?) {
        val sectionIndex = y.sectionHeight - minSection

        var blocks = blocks[sectionIndex]
        if (blocks == null) {
            if (state == null) return
            blocks = arrayOfNulls(ChunkSize.BLOCKS_PER_SECTION)
            this.blocks[sectionIndex] = blocks
        }

        blocks[InSectionPosition(x, y.inSectionHeight, z).index] = state
    }


    fun toData() = ChunkData(blocks, biomeSource = biomes)
}
