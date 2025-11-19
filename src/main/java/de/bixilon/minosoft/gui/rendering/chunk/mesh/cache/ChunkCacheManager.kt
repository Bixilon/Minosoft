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

import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantLock
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class ChunkCacheManager(
    val renderer: ChunkRenderer,
) {
    private val caches: HashMap<ChunkPosition, Int2ObjectOpenHashMap<ChunkMeshCache>> = HashMap(100)
    private val lock = ReentrantLock()

    fun clear() = lock.locked {
        for ((position, sections) in caches) {
            renderer.unloadingQueue += sections.values
        }
        this.caches.clear()
    }

    operator fun set(position: SectionPosition, cache: ChunkMeshCache) = lock.locked {
        val previous = this.caches.getOrPut(position.chunkPosition) { Int2ObjectOpenHashMap() }.put(position.y, cache) ?: return@locked

        renderer.unloadingQueue += previous
    }

    private fun remove(position: SectionPosition): ChunkMeshCache? = lock.locked {
        val sections = this.caches[position.chunkPosition] ?: return@locked null
        val cache = sections.remove(position.y) ?: return@locked null

        if (sections.isEmpty()) {
            this.caches -= position.chunkPosition
        }

        return@locked cache
    }

    operator fun get(position: SectionPosition) = remove(position)

    operator fun minusAssign(position: SectionPosition) {
        val cache = remove(position) ?: return
        renderer.unloadingQueue += cache
    }

    operator fun minusAssign(position: ChunkPosition) {
        val sections = lock.locked { caches.remove(position) } ?: return

        renderer.unloadingQueue += sections.values
    }
}
