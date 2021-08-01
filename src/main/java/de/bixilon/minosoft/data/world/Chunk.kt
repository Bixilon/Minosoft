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

import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.world.biome.source.BiomeSource
import de.bixilon.minosoft.data.world.light.LightAccessor
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkSectionPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import glm_.vec2.Vec2i
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

    operator fun get(inChunkPosition: Vec3i): BlockState? {
        return sections?.get(inChunkPosition.sectionHeight)?.getBlockState(inChunkPosition.inChunkSectionPosition)
    }

    operator fun get(x: Int, y: Int, z: Int): BlockState? {
        return get(Vec3i(x, y, z))
    }

    fun setBlocks(blocks: Map<Vec3i, BlockState?>) {
        for ((location, blockInfo) in blocks) {
            set(location, blockInfo)
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
                    getOrPut(sectionHeight).setData(chunkSection)
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


    operator fun set(inChunkPosition: Vec3i, blockState: BlockState?) {
        getOrPut(inChunkPosition.sectionHeight).setBlockState(inChunkPosition.inChunkSectionPosition, blockState)
    }

    private fun getOrPut(sectionHeight: Int): ChunkSection {
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

    fun realTick(connection: PlayConnection, chunkPosition: Vec2i) {
        if (!isFullyLoaded) {
            return
        }
        val sections = sections
        sections ?: return
        for ((height, section) in sections.toSynchronizedMap()) {
            section.realTick(connection, Vec3i.of(chunkPosition, height, Vec3i.EMPTY))
        }
    }


    fun getBlockEntity(inChunkPosition: Vec3i): BlockEntity? {
        return sections?.get(inChunkPosition.sectionHeight)?.getBlockEntity(inChunkPosition.inChunkSectionPosition)
    }

    operator fun set(inChunkPosition: Vec3i, blockEntity: BlockEntity?) {
        sections?.get(inChunkPosition.sectionHeight)?.setBlockEntity(inChunkPosition.inChunkSectionPosition, blockEntity)
    }
}
