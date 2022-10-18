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

import de.bixilon.kutil.benchmark.BenchmarkUtil
import de.bixilon.kutil.unit.UnitFormatter.formatNanos
import de.bixilon.minosoft.data.world.chunk.ChunkTestingUtil.createChunkWithNeighbours
import de.bixilon.minosoft.data.world.chunk.ChunkTestingUtil.createSolidBlock
import de.bixilon.minosoft.data.world.chunk.ChunkTestingUtil.createSolidLight
import de.bixilon.minosoft.data.world.chunk.ChunkTestingUtil.fillBottom
import org.testng.annotations.Test
import kotlin.system.measureNanoTime


internal class LightBenchmark {

    @Test
    fun calculateEmptyLight() {
        val chunk = createChunkWithNeighbours()
        BenchmarkUtil.benchmark(100000) {
            chunk.light.recalculate()
        }.println()
    }

    @Test
    fun calculateWithSolidBottom() {
        val chunk = createChunkWithNeighbours()
        chunk.fillBottom(createSolidBlock().defaultState)
        BenchmarkUtil.benchmark(100000) {
            chunk.light.recalculate()
        }.println()
    }

    // same tests like https://github.com/PaperMC/Starlight
    @Test
    fun calculateSimplePlace() {
        val chunk = createChunkWithNeighbours()
        val solid = createSolidBlock().defaultState
        val light = createSolidLight().defaultState
        val lowest = chunk.getOrPut(0)!!.blocks
        for (index in 0 until 256) {
            lowest.unsafeSet(index, solid)
        }
        val highest = chunk.getOrPut(15)!!.blocks
        for (index in 0 until 256) {
            highest.unsafeSet(index or (0x0F shl 8), solid)
        }
        var totalPlace = 0L
        var totalBreak = 0L
        val benchmark = BenchmarkUtil.benchmark(10000) {
            totalPlace += measureNanoTime { chunk.set(7, 1, 7, light) }
            totalBreak += measureNanoTime { chunk.set(7, 1, 7, null) }
        }

        println("Placing light took ${totalPlace.formatNanos()}, avg=${(totalPlace / benchmark.runs).formatNanos()}, runs=${benchmark.runs}")
        println("Breaking light took ${totalBreak.formatNanos()}, avg=${(totalBreak / benchmark.runs).formatNanos()}, runs=${benchmark.runs}")
    }

    @Test
    fun calculateChangeAtY255() {
        val chunk = createChunkWithNeighbours()
        val solid = createSolidBlock().defaultState
        val lowest = chunk.getOrPut(0)!!.blocks
        for (index in 0 until 256) {
            lowest.unsafeSet(index, solid)
        }
        val highest = chunk.getOrPut(15)!!.blocks
        for (index in 0 until 256) {
            highest.unsafeSet(index or (0x0F shl 8), solid)
        }
        var totalPlace = 0L
        var totalBreak = 0L
        val benchmark = BenchmarkUtil.benchmark(10000) {
            totalBreak += measureNanoTime { chunk.set(7, 255, 7, null) }
            totalPlace += measureNanoTime { chunk.set(7, 255, 7, solid) }
        }

        println("Placing block took ${totalPlace.formatNanos()}, avg=${(totalPlace / benchmark.runs).formatNanos()}, runs=${benchmark.runs}")
        println("Breaking block took ${totalBreak.formatNanos()}, avg=${(totalBreak / benchmark.runs).formatNanos()}, runs=${benchmark.runs}")
    }
}
