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

package de.bixilon.minosoft.local.generator

import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.world.biome.source.DummyBiomeSource
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import kotlin.math.sqrt

class DebugGenerator(val session: PlaySession) : ChunkGenerator {
    private var plains = session.registries.biome[minecraft("plains")]
    private var size = (sqrt(session.registries.blockState.size.toFloat())).toInt() + 1


    override fun generate(builder: ChunkBuilder) {
        builder.biomes = DummyBiomeSource(plains)
        if (builder.position.x < 0 || builder.position.z < 0) return

        val xOffset = builder.position.x * ChunkSize.SECTION_WIDTH_X
        val zOffset = builder.position.z * ChunkSize.SECTION_WIDTH_Z

        for (x in 0 until ChunkSize.SECTION_WIDTH_X step 2) {
            val actuallyX = (xOffset + x) / 2
            if (actuallyX > size) continue

            for (z in 0 until ChunkSize.SECTION_WIDTH_Z step 2) {
                val actuallyZ = (zOffset + z) / 2
                if (actuallyZ > size) continue

                val id = actuallyX * size + actuallyZ
                val state = session.registries.blockState.getOrNull(id) ?: continue
                builder[x, 8, z] = state
            }
        }
    }
}
