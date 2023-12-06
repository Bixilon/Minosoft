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

package de.bixilon.minosoft.data.world.biome.accessor.noise

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_19W36A

abstract class NoiseBiomeAccessor(
    connection: PlayConnection,
    val seed: Long = 0L,
) {
    protected val world = connection.world
    protected var fastNoise = false

    init {
        val profile = connection.profiles.rendering
        profile.performance::fastBiomeNoise.observe(this, true) { fastNoise = it }
    }

    abstract fun get(x: Int, y: Int, z: Int, chunkPositionX: Int, chunkPositionZ: Int, chunk: Chunk, neighbours: Array<Chunk>?): Biome?

    fun get(x: Int, y: Int, z: Int, chunk: Chunk): Biome? {
        val neighbours = chunk.neighbours.get() ?: return null
        return get((chunk.chunkPosition.x shl 4) or x, y, (chunk.chunkPosition.y shl 4) or z, chunk.chunkPosition.x, chunk.chunkPosition.y, chunk, neighbours)
    }


    companion object {

        fun get(connection: PlayConnection, seed: Long): NoiseBiomeAccessor? = when {
            connection.version < V_19W36A -> null
            else -> VoronoiBiomeAccessor(connection, seed)
        }
    }
}
