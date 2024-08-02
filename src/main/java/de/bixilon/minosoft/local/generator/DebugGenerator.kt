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

package de.bixilon.minosoft.local.generator

import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.world.biome.source.DummyBiomeSource
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import kotlin.math.sqrt

class DebugGenerator(val session: PlaySession) : ChunkGenerator {
    private var plains = session.registries.biome[minecraft("plains")]
    private var size = (sqrt(session.registries.blockState.size.toFloat())).toInt() + 1


    override fun generate(chunk: Chunk) {
        chunk.biomeSource = DummyBiomeSource(plains)
        if (chunk.chunkPosition.x < 0 || chunk.chunkPosition.y < 0) return

        val xOffset = chunk.chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X
        val zOffset = chunk.chunkPosition.y * ProtocolDefinition.SECTION_WIDTH_Z

        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X step 2) {
            if (xOffset + x > size) continue
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z step 2) {
                if (zOffset + z > size) continue
                val id = (xOffset + x) / 2 * size + (zOffset + z) / 2
                chunk[x, 8, z] = session.registries.blockState.getOrNull(id)
            }
        }
    }

    private fun fillDebug() {
        val total = session.registries.blockState.size
        val width = sqrt(total.toFloat()).toInt() + 1
        var count = 0

        for (block in session.registries.block) {
            for (state in block.states) {
                val x = (count / width - (width / 2)) * 2
                val z = (count % width - (width / 2)) * 2
                val chunk = session.world.chunks.create(ChunkPosition(x shr 4, z shr 4), biome = DummyBiomeSource(plains))
                chunk[x and 0x0F, 8, z and 0x0F] = state
                count++
            }
        }
    }
}
