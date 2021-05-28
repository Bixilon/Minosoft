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
import glm_.vec3.Vec3i

interface BlockEntityProvider {
    val size: Int

    operator fun get(inChunkSectionPosition: Vec3i): BlockEntity?

    operator fun set(inChunkSectionPosition: Vec3i, blockEntity: BlockEntity?)

    fun clone(): BlockEntityProvider

    fun forEach(lambda: (entity: BlockEntity, inChunkSectionPosition: Vec3i) -> Unit)

    companion object {
        const val BLOCK_ENTITY_MAP_LIMIT_UP = 15
        const val BLOCK_ENTITY_MAP_LIMIT_DOWN = 5
    }
}
