/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.world

import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkSectionPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import glm_.vec3.Vec3i

/**
 * Collection of chunks sections (allocated in y)
 */
class Chunk(
    var sections: MutableMap<Int, ChunkSection>? = null,
    var biomeSource: BiomeSource? = null,
    var lightAccessor: LightAccessor? = null,
) {
    private val lock = Object()
    val isFullyLoaded: Boolean
        get() {
            return sections != null && biomeSource != null && lightAccessor != null
        }

    fun getBlockState(inChunkPosition: Vec3i): BlockState? {
        return sections?.get(inChunkPosition.sectionHeight)?.getBlockState(inChunkPosition.inChunkSectionPosition)
    }

    fun getBlockState(x: Int, y: Int, z: Int): BlockState? {
        return getBlockState(Vec3i(x, y, z))
    }

    fun setBlocks(blocks: HashMap<Vec3i, BlockState?>) {
        for ((location, blockInfo) in blocks) {
            setBlockState(location, blockInfo)
        }
    }

    fun setData(data: ChunkData, merge: Boolean = false) {
        synchronized(lock) {
            data.blocks?.let {
                if (sections == null) {
                    sections = mutableMapOf()
                }
                if (!merge) {
                    sections?.clear()
                }
                // replace all chunk sections
                for ((sectionHeight, chunkSection) in it) {
                    getSectionOrCreate(sectionHeight).setData(chunkSection)
                }
            }
            data.biomeSource?.let {
                this.biomeSource = it
            }
            data.lightAccessor?.let {
                this.lightAccessor = it
            }
        }
    }

    fun setRawBlocks(blocks: Map<Vec3i, BlockState?>) {
        for ((location, blockState) in blocks) {
            setBlockState(location, blockState)
        }
    }

    fun setBlockState(inChunkPosition: Vec3i, blockState: BlockState?) {
        getSectionOrCreate(inChunkPosition.sectionHeight).setBlockState(inChunkPosition.inChunkSectionPosition, blockState)
    }

    fun getSectionOrCreate(sectionHeight: Int): ChunkSection {
        if (sections == null) {
            throw IllegalStateException("Chunk not received/initialized yet!")
        }
        return sections!![sectionHeight].let {
            var section = it
            if (section == null) {
                section = ChunkSection()
                sections!![sectionHeight] = section
            }
            section
        }
    }
}
