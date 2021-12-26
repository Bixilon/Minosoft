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

import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.world.ChunkSection.Companion.indexPosition
import glm_.vec3.Vec3i

class MapBlockEntityProvider(
    var blockEntities: MutableMap<Vec3i, BlockEntity> = synchronizedMapOf(),
) : BlockEntityProvider {
    override val size: Int
        get() = blockEntities.size

    constructor(blockEntityProvider: ArrayBlockEntityProvider) : this() {
        for ((index, blockEntity) in blockEntityProvider.blockEntities.withIndex()) {
            if (blockEntity == null) {
                continue
            }
            blockEntities[index.indexPosition] = blockEntity
        }
    }

    override fun get(inChunkSectionPosition: Vec3i): BlockEntity? {
        return blockEntities[inChunkSectionPosition]
    }

    override fun set(inChunkSectionPosition: Vec3i, blockEntity: BlockEntity?) {
        if (blockEntity == null) {
            blockEntities.remove(inChunkSectionPosition)
            return
        }
        blockEntities[inChunkSectionPosition] = blockEntity
    }

    override fun clone(): MapBlockEntityProvider {
        return MapBlockEntityProvider(blockEntities.toMutableMap())
    }

    override fun forEach(lambda: (entity: BlockEntity, inChunkSectionPosition: Vec3i) -> Unit) {
        for ((position, blockEntity) in blockEntities.toSynchronizedMap()) {
            lambda(blockEntity, position)
        }
    }
}
