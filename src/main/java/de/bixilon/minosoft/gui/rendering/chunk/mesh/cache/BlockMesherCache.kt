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

package de.bixilon.minosoft.gui.rendering.chunk.mesh.cache

import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer

class BlockMesherCache(
    val context: RenderContext,
) {
    private var entities: BlockEntityCacheState? = null

    fun createEntity(position: InSectionPosition, entity: BlockEntity): BlockEntityRenderer? {
        var renderer: BlockEntityRenderer? = null
        var entities = this.entities
        if (entities == null) {
            // no cache yet
            renderer = entity.createRenderer(context) ?: return null
            entities = BlockEntityCacheState()
            this.entities = entities
            entities.store(position, renderer)
        }
        renderer = renderer ?: entities.entities[position.index]

        if (renderer == null) {
            // cached version is empty (or already created when cache was empty
            renderer = entity.createRenderer(context) ?: return null
            entities.store(position, renderer)
        } else {
            entities.usage[position.index] = true
        }

        return renderer
    }

    fun unmark() {
        entities?.usage?.clear()
    }

    fun cleanup() {
        val entities = entities ?: return

        val remove = ArrayList<BlockEntityRenderer>(minOf(16, entities.count))

        for (index in 0 until ChunkSize.BLOCKS_PER_SECTION) {
            val entity = entities.entities[index] ?: continue
            if (entities.usage[index]) continue

            entities.entities[index] = null
            remove += entity
            entities.count--
        }
        if (entities.count == 0) {
            this.entities = null
        }

        context.queue += { remove.forEach { it.unload() } }
    }

    fun unload() {
        entities?.entities?.forEach { it?.unload() }
        entities = null
    }

    fun drop() {
        entities?.entities?.forEach { it?.drop() }
        entities = null
    }
}
