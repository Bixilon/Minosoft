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
import de.bixilon.kutil.collections.iterator.async.ConcurrentIterator
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.ChunkUtil.isInViewDistance
import java.util.*

class ChunkTicker(val manager: ChunkManager) {
    private var random: Random = manager.world.random


    fun tick(simulationDistance: Int, cameraPosition: Vec2i) {
        val iterator = ConcurrentIterator(manager.chunks.unsafe.entries.spliterator(), priority = ThreadPool.HIGH)
        iterator.iterate {
            if (!it.key.isInViewDistance(simulationDistance, cameraPosition)) {
                return@iterate
            }
            it.value.tick(manager.world.connection, it.key, random)
        }
    }
}
