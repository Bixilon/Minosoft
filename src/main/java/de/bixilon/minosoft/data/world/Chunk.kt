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
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i

/**
 * Collection of chunks sections (from the lowest section to the highest section in y axis)
 */
class Chunk(
    private val connection: PlayConnection,
    private var sections: Array<ChunkSection?>? = null,
    var biomeSource: BiomeSource? = null,
    var lightAccessor: LightAccessor? = null,
) : Iterable<ChunkSection?> {
    val lowestSection = connection.world.dimension!!.lowestSection

    val blocksInitialized: Boolean
        get() = sections != null
    val biomesInitialized
        get() = biomeSource != null
    val lightInitialized
        get() = lightAccessor != null

    val isFullyLoaded: Boolean
        get() = blocksInitialized && biomesInitialized && lightInitialized

    operator fun get(sectionHeight: Int): ChunkSection? = sections?.getOrNull(sectionHeight - lowestSection)

    fun get(x: Int, y: Int, z: Int): BlockState? {
        return this[y.sectionHeight]?.blocks?.get(x, y % ProtocolDefinition.SECTION_HEIGHT_Y, z)
    }

    operator fun get(position: Vec3i): BlockState? = get(position.x, position.y, position.z)

    fun set(x: Int, y: Int, z: Int, blockState: BlockState?, blockEntity: BlockEntity? = null) {
        val section = getOrPut(y.sectionHeight)
        section.blocks[x, y % ProtocolDefinition.SECTION_HEIGHT_Y, z] = blockState
        section.blockEntities[x, y % ProtocolDefinition.SECTION_HEIGHT_Y, z] = blockEntity // ToDo
    }

    operator fun set(position: Vec3i, blockState: BlockState?) = set(position.x, position.y, position.z, blockState)

    fun setBlocks(blocks: Map<Vec3i, BlockState?>) {
        for ((location, blockState) in blocks) {
            set(location, blockState)
        }
    }

    fun getBlockEntity(x: Int, y: Int, z: Int): BlockEntity? {
        return this[y.sectionHeight]?.blockEntities?.get(x, y % ProtocolDefinition.SECTION_HEIGHT_Y, z)
    }

    fun getBlockEntity(position: Vec3i): BlockEntity? = getBlockEntity(position.x, position.y, position.z)

    fun setBlockEntity(x: Int, y: Int, z: Int, blockEntity: BlockEntity?) {
        getOrPut(y.sectionHeight).blockEntities[x, y % ProtocolDefinition.SECTION_HEIGHT_Y, z] = blockEntity
    }

    fun setBlockEntity(position: Vec3i, blockEntity: BlockEntity?) = setBlockEntity(position.x, position.y, position.z, blockEntity)

    fun setData(data: ChunkData, merge: Boolean = false) {
        data.blocks?.let {
            var sections = this.sections
            if (sections == null || !merge) {
                sections = arrayOfNulls(connection.world.dimension!!.sections)
                this.sections = sections
            }

            // replace all chunk sections
            for ((index, section) in it.withIndex()) {
                section ?: continue
                sections[index] = section
            }
        }
        data.biomeSource?.let {
            this.biomeSource = it
        }
        data.lightAccessor?.let {
            this.lightAccessor = it
        }
    }

    private fun getOrPut(sectionHeight: Int): ChunkSection {
        val sections = sections ?: throw NullPointerException("Sections not initialized yet!")
        val sectionIndex = sectionHeight - lowestSection

        var section = sections[sectionIndex]
        if (section == null) {
            section = ChunkSection(connection.registries)
            sections[sectionIndex] = section
        }
        return section
    }

    fun tick(connection: PlayConnection, chunkPosition: Vec2i) {
        if (!isFullyLoaded) {
            return
        }
        val sections = sections!!
        for ((index, section) in sections.withIndex()) {
            section ?: continue
            section.tick(connection, chunkPosition, index - lowestSection)
        }
    }

    override fun iterator(): Iterator<ChunkSection?> {
        return sections!!.iterator()
    }
}
