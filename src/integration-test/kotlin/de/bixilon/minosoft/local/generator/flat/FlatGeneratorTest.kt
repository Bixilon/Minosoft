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

import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.blocks.state.TestBlockStates
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkData
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.local.generator.ChunkBuilder
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["chunk"])
class FlatGeneratorTest {

    private fun FlatGenerator.build(minSection: Int = 0): ChunkData {
        val world = World::class.java.allocate()
        world::dimension.forceSet(DataObserver(DimensionProperties(minY = minSection.sectionHeight)))

        val builder = ChunkBuilder(world, ChunkPosition(0, 0))

        this.generate(builder)

        return builder.toData()
    }

    fun `single layer`() {
        val generator = FlatGenerator(null, arrayOf(TestBlockStates.TEST1))
        val data = generator.build()

        val expected = Array(16) { if (it == 0) Array(ChunkSize.BLOCKS_PER_SECTION) { if (it < 256) TestBlockStates.TEST1 else null } else null }

        assertEquals(data.blocks, expected)
    }

    // TODO: multiple layers, multiple sections, biomes, different minSection
}
