/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.block.entities

import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.world.ChunkSection.Companion.index
import de.bixilon.minosoft.data.world.ChunkSection.Companion.indexPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3i

class ArrayBlockEntityProvider(
    var blockEntities: Array<BlockEntity?> = arrayOfNulls(ProtocolDefinition.BLOCKS_PER_SECTION),
) : BlockEntityProvider {
    override var size: Int = 0
        private set

    constructor(blockEntityProvider: MapBlockEntityProvider) : this() {
        for ((position, blockEntity) in blockEntityProvider.blockEntities) {
            blockEntities[position.index] = blockEntity
        }
    }

    override fun get(inChunkSectionPosition: Vec3i): BlockEntity? {
        return blockEntities[inChunkSectionPosition.index]
    }

    override fun set(inChunkSectionPosition: Vec3i, blockEntity: BlockEntity?) {
        val previous = blockEntities[inChunkSectionPosition.index]
        blockEntities[inChunkSectionPosition.index] = blockEntity
        if (previous != null && blockEntity == null) {
            size--
        } else if (previous == null && blockEntity != null) {
            size++
        }
    }

    override fun clone(): ArrayBlockEntityProvider {
        return ArrayBlockEntityProvider(blockEntities.clone())
    }

    override fun forEach(lambda: (entity: BlockEntity, inChunkSectionPosition: Vec3i) -> Unit) {
        for ((index, blockEntity) in blockEntities.withIndex()) {
            blockEntity ?: continue
            lambda(blockEntity, index.indexPosition)
        }
    }
}
