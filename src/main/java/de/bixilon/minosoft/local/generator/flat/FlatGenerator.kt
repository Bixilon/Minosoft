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

package de.bixilon.minosoft.local.generator.flat

import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.biome.source.DummyBiomeSource
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.local.generator.ChunkGenerator
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

open class FlatGenerator(
    val biome: Biome?,
    val layers: Array<BlockState?>,
) : ChunkGenerator {

    override fun generate(chunk: Chunk) {
        chunk.biomeSource = DummyBiomeSource(biome)

        val minY = chunk.world.dimension.minY
        for ((index, layer) in layers.withIndex()) {
            if (layer == null) continue

            for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                    chunk[x, index + minY, z] = layer // TODO: batch updates
                }
            }
        }
    }
}
