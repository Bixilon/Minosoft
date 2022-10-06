/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.chunk.light

import de.bixilon.minosoft.data.world.chunk.ChunkTestingUtil.createChunkWithNeighbours
import de.bixilon.minosoft.data.world.chunk.ChunkTestingUtil.createSolidBlock
import de.bixilon.minosoft.data.world.chunk.ChunkTestingUtil.fillBottom
import de.bixilon.minosoft.util.benchmark.BenchmarkUtil
import org.junit.jupiter.api.Test


internal class LightBenchmark {

    @Test
    fun calculateEmptyLight() {
        val chunk = createChunkWithNeighbours()
        BenchmarkUtil.benchmark(1000000) {
            chunk.light.recalculate()
        }.println()
    }

    @Test
    fun calculateWithSolidBottom() {
        val chunk = createChunkWithNeighbours()
        chunk.fillBottom(createSolidBlock().defaultState)
        BenchmarkUtil.benchmark(1000000) {
            chunk.light.recalculate()
        }.println()
    }
}
