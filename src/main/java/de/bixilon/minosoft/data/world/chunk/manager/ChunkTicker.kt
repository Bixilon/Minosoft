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

package de.bixilon.minosoft.data.world.chunk.manager

import de.bixilon.kutil.collections.iterator.AsyncIteration.async
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.minosoft.data.world.chunk.ChunkUtil.isInViewDistance
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import java.util.*

class ChunkTicker(val manager: ChunkManager) {
    private var random: Random = manager.world.random


    fun tick(simulationDistance: Int, cameraPosition: ChunkPosition) {
        manager.chunks.unsafe.entries.async(priority = ThreadPool.HIGH) {
            if (!it.key.isInViewDistance(simulationDistance, cameraPosition)) {
                return@async
            }
            it.value.tick(manager.world.session, random)
        }
    }
}
