/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.chunk.manager

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.collections.iterator.async.QueuedIterator
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.util.chunk.ChunkUtil.isInViewDistance
import java.util.*

class ChunkTicker(val manager: ChunkManager) {
    private val iterator = QueuedIterator(manager.chunks.unsafe.entries.spliterator(), priority = ThreadPool.HIGH, queueSize = 1000, executor = createTicker())
    private var simulationDistance = 0
    private var cameraPosition = Vec2i.EMPTY
    private var random: Random = manager.world.random


    private fun createTicker(): (Map.Entry<ChunkPosition, Chunk>) -> Unit = tick@{
        if (!it.key.isInViewDistance(simulationDistance, cameraPosition)) {
            return@tick
        }
        it.value.tick(manager.world.connection, it.key, random)
    }


    fun tick(simulationDistance: Int, cameraPosition: Vec2i) {
        this.simulationDistance = simulationDistance
        this.cameraPosition = cameraPosition
        iterator.reuse(manager.chunks.unsafe.entries.spliterator())
        iterator.iterate()
    }
}
