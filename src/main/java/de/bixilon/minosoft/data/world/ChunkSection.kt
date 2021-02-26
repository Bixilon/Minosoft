/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.world

import de.bixilon.minosoft.data.mappings.blocks.BlockState

/**
 * Collection of 16x16x16 blocks
 */
class ChunkSection constructor(
    val blocks: MutableMap<InChunkSectionPosition, BlockInfo> = mutableMapOf(),
) {

    fun getBlockInfo(position: InChunkSectionPosition): BlockInfo? {
        return blocks[position]
    }

    fun setBlockInfo(position: InChunkSectionPosition, blockInfo: BlockInfo?) {
        if (blockInfo == null) {
            blocks.remove(position)
            return
        }
        blocks[position] = blockInfo
    }

    fun getBlockInfo(x: Int, y: Int, z: Int): BlockInfo? {
        return getBlockInfo(InChunkSectionPosition(x, y, z))
    }

    fun setRawBlock(position: InChunkSectionPosition, block: BlockState?) {
        if (block == null) {
            setBlockInfo(position, null)
            return
        }
        setBlockInfo(position, BlockInfo(block))
    }

    fun setData(chunkSection: ChunkSection, merge: Boolean = false) {
        if (!merge) {
            this.blocks.clear()
        }
        this.blocks.putAll(chunkSection.blocks)
    }
}
