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

package de.bixilon.minosoft.local.generator.flat

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.building.dirt.GrassBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.Bedrock
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.world.biome.source.DummyBiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.local.generator.ChunkGenerator
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class FlatGenerator(
    val biome: Biome?,
    val layers: Array<BlockState?>,
) : ChunkGenerator {

    override fun generate(chunk: Chunk) {
        chunk.biomeSource = DummyBiomeSource(biome)

        val minY = chunk.world.dimension.minY
        for ((index, layer) in layers.withIndex()) {
            if (layer == null) continue

            for (x in 0 until ChunkSize.SECTION_WIDTH_X) {
                for (z in 0 until ChunkSize.SECTION_WIDTH_Z) {
                    chunk[InChunkPosition(x, index + minY, z)] = layer
                }
            }
        }
    }

    companion object {

        fun default(session: PlaySession): FlatGenerator {
            val bedrock = session.registries.block[Bedrock]?.states?.default
            val stone = session.registries.block[StoneBlock.Block]?.states?.default
            val grass = session.registries.block[GrassBlock]?.states?.default

            val plains = session.registries.biome[minecraft("plains")]

            return FlatGenerator(plains, arrayOf(
                bedrock,
                stone,
                stone,
                stone,
                stone,
                stone,
                grass,
            ))
        }
    }
}
