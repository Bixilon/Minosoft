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

package de.bixilon.minosoft.data.registries.blocks.types.building.nether

import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["block"])
class SoulSandTest {
    private val block by lazy { IT.REGISTRIES.block[SoulSand] ?: throw SkipException("") }

    fun `block state flags`() {
        assertTrue(BlockStateFlags.COLLISIONS in block.states.default.flags)
        assertTrue(BlockStateFlags.VELOCITY in block.states.default.flags)

        assertTrue(BlockStateFlags.FULL_OPAQUE in block.states.default.flags)

        assertTrue(BlockStateFlags.OUTLINE in block.states.default.flags)
        assertTrue(BlockStateFlags.FULL_OUTLINE in block.states.default.flags)

        assertFalse(BlockStateFlags.FULL_COLLISION in block.states.default.flags)

        assertFalse(BlockStateFlags.ENTITY !in block.states.default.flags)
    }
}
