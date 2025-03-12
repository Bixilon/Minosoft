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

package de.bixilon.minosoft.data.world.chunk.light

import de.bixilon.kutil.benchmark.BenchmarkUtil
import de.bixilon.kutil.unit.UnitFormatter.format
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.LightTestingUtil.createChunkWithNeighbours
import de.bixilon.minosoft.data.world.chunk.LightTestingUtil.createOpaqueLight
import de.bixilon.minosoft.data.world.chunk.LightTestingUtil.createSolidBlock
import de.bixilon.minosoft.data.world.chunk.LightTestingUtil.fillBottom
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import org.testng.annotations.Test
import kotlin.time.Duration
import kotlin.time.measureTime


internal class LightBenchmark {

    @Test
    fun calculateEmptyLight() {
        val chunk = createChunkWithNeighbours()
        BenchmarkUtil.benchmark(100000) {
            chunk.light.clear()
            chunk.light.calculate()
        }.println()
    }

    @Test
    fun calculateWithSolidBottom() {
        val chunk = createChunkWithNeighbours()
        chunk.fillBottom(createSolidBlock().states.default)
        BenchmarkUtil.benchmark(100000) {
            chunk.light.clear()
            chunk.light.calculate()
        }.println()
    }

    // same tests like https://github.com/PaperMC/Starlight
    @Test
    fun calculateSimplePlace() {
        val chunk = createChunkWithNeighbours()
        val solid = createSolidBlock().states.default
        val light = createOpaqueLight().states.default
        val lowest = chunk.getOrPut(0)!!.blocks
        for (z in 0 until ChunkSize.SECTION_WIDTH_Z) {
            for (x in 0 until ChunkSize.SECTION_WIDTH_X) {
                lowest.unsafeSet(InSectionPosition(x, 0, z), solid)
            }
        }
        val highest = chunk.getOrPut(15)!!.blocks

        for (z in 0 until ChunkSize.SECTION_WIDTH_Z) {
            for (x in 0 until ChunkSize.SECTION_WIDTH_X) {
                highest.unsafeSet(InSectionPosition(x, ChunkSize.SECTION_MAX_Y, z), solid)
            }
        }
        var totalPlace = Duration.ZERO
        var totalBreak = Duration.ZERO
        val benchmark = BenchmarkUtil.benchmark(10000) {
            totalPlace += measureTime { chunk[InChunkPosition(7, 1, 7)] = light }
            totalBreak += measureTime { chunk[InChunkPosition(7, 1, 7)] = null }
        }

        println("Placing light took ${totalPlace.format()}, avg=${(totalPlace / benchmark.iterations).format()}, runs=${benchmark.iterations}")
        println("Breaking light took ${totalBreak.format()}, avg=${(totalBreak / benchmark.iterations).format()}, runs=${benchmark.iterations}")
    }

    @Test
    fun calculateChangeAtY255() {
        val chunk = createChunkWithNeighbours()
        val solid = createSolidBlock().states.default
        val lowest = chunk.getOrPut(0)!!.blocks
        for (z in 0 until ChunkSize.SECTION_WIDTH_Z) {
            for (x in 0 until ChunkSize.SECTION_WIDTH_X) {
                lowest.unsafeSet(InSectionPosition(x, 0, z), solid)
            }
        }
        val highest = chunk.getOrPut(15)!!.blocks
        for (z in 0 until ChunkSize.SECTION_WIDTH_Z) {
            for (x in 0 until ChunkSize.SECTION_WIDTH_X) {
                highest.unsafeSet(InSectionPosition(x, ChunkSize.SECTION_MAX_Y, z), solid)
            }
        }

        var totalPlace = Duration.ZERO
        var totalBreak = Duration.ZERO
        val benchmark = BenchmarkUtil.benchmark(100000) {
            totalBreak += measureTime { chunk[InChunkPosition(7, 255, 7)] = null }
            totalPlace += measureTime { chunk[InChunkPosition(7, 255, 7)] = solid }
        }

        println("Placing block took ${totalPlace.format()}, avg=${(totalPlace / benchmark.iterations).format()}, runs=${benchmark.iterations}")
        println("Breaking block took ${totalBreak.format()}, avg=${(totalBreak / benchmark.iterations).format()}, runs=${benchmark.iterations}")
    }

    @Test
    fun placeBottom() {
        val chunk = createChunkWithNeighbours()

        val solid = createSolidBlock().states.default
        val light = createOpaqueLight().states.default
        val highest = chunk.getOrPut(15)!!.blocks
        for (z in 0 until ChunkSize.SECTION_WIDTH_Z) {
            for (x in 0 until ChunkSize.SECTION_WIDTH_X) {
                highest.unsafeSet(InSectionPosition(x, ChunkSize.SECTION_MAX_Y, z), solid)
            }
        }
        var totalPlace = Duration.ZERO
        var totalBreak = Duration.ZERO
        val benchmark = BenchmarkUtil.benchmark(10000) {
            totalPlace += measureTime { chunk[InChunkPosition(8, 0, 8)] = light }
            totalBreak += measureTime { chunk[InChunkPosition(8, 0, 8)] = null }
        }

        println("Placing block took ${totalPlace.format()}, avg=${(totalPlace / benchmark.iterations).format()}, runs=${benchmark.iterations}")
        println("Breaking block took ${totalBreak.format()}, avg=${(totalBreak / benchmark.iterations).format()}, runs=${benchmark.iterations}")
    }
}
