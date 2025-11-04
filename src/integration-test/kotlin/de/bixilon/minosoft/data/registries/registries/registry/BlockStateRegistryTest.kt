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

package de.bixilon.minosoft.data.registries.registries.registry

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.air.AirBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.assertNull
import org.testng.Assert.assertSame
import org.testng.annotations.Test

@Test(groups = ["registry"])
class BlockStateRegistryTest {
    private val a = BlockState::class.java.allocate().apply { this::block.forceSet(StoneBlock.Block::class.java.allocate()) }
    private val b = BlockState::class.java.allocate().apply { this::block.forceSet(StoneBlock.Block::class.java.allocate()) }
    private val c = BlockState::class.java.allocate().apply { this::block.forceSet(AirBlock.Air::class.java.allocate()) }

    private fun create(): BlockStateRegistry {
        val registry = BlockStateRegistry(true)
        registry[0] = a
        registry[1] = b
        registry[2] = c

        return registry
    }

    fun `0 is always null`() {
        assertNull(create().getOrNull(0))
    }

    fun `air block is always null`() {
        assertNull(create().getOrNull(2))
    }

    fun `everything else`() {
        assertSame(create().getOrNull(1), b)
    }

    // TODO: pre flattening test
}
