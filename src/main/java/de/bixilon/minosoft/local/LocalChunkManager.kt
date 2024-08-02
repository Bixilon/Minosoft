/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.local

import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.local.generator.ChunkGenerator
import de.bixilon.minosoft.local.storage.WorldStorage
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.ChunkUtil.isInViewDistance

class LocalChunkManager(
    val session: PlaySession,
    val storage: WorldStorage,
    val generator: ChunkGenerator,
) {

    fun update() {
        val position = session.player.physics.positionInfo.chunkPosition
        val distance = session.profiles.block.viewDistance

        // TODO: This is rather slow and only catches the player, but works
        unloadAll(position, distance)
        load(position, distance)
    }

    private fun unloadAll(center: ChunkPosition, distance: Int) {
        session.world.lock.lock()
        val unload: MutableSet<ChunkPosition> = mutableSetOf()
        for ((position, _) in session.world.chunks.chunks.unsafe) {
            if (position.isInViewDistance(distance, center)) continue
            unload += position
        }
        session.world.lock.unlock()
        for (position in unload) {
            session.world.chunks.unload(position)
        }
    }

    private fun load(center: ChunkPosition, distance: Int) {
        for (x in center.x - distance..center.x + distance) {
            for (z in center.y - distance..center.y + distance) {
                val position = ChunkPosition(x, z)
                var chunk = session.world.chunks[position]
                if (chunk != null) continue
                chunk = session.world.chunks.create(position)
                generator.generate(chunk)
            }
        }
    }
}
