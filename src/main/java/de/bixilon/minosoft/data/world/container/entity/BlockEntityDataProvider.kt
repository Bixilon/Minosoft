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

package de.bixilon.minosoft.data.world.container.entity

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.minosoft.data.Tickable
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.container.SectionDataProvider
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition

class BlockEntityDataProvider(
    lock: Lock? = null,
    val section: ChunkSection,
) : SectionDataProvider<BlockEntity>(lock, false), Tickable {

    override fun create() = arrayOfNulls<BlockEntity?>(ChunkSize.BLOCKS_PER_SECTION)

    fun update(position: InSectionPosition): BlockEntity? {
        this[position]?.let { return it }
        val state = section.blocks[position] ?: return null

        if (BlockStateFlags.ENTITY !in state.flags) return null
        val block = state.block.unsafeCast<BlockWithEntity<*>>()
        val entity = block.createBlockEntity(section.chunk.session, BlockPosition.of(section.chunk.position, section.height, position), state) ?: return null

        // TODO: potential race condition
        this[position] = entity

        return entity
    }


    override fun tick() = forEach { _, entity -> entity.tick() }
}
