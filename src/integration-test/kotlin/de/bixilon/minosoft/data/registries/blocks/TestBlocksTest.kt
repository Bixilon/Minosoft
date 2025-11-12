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

package de.bixilon.minosoft.data.registries.blocks

import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

@Test(groups = ["blocks"])
class TestBlocksTest {

    fun `opaque are all opaque`() {
        assertTrue(BlockStateFlags.FULL_OPAQUE in TestBlocks.OPAQUE1.flags)
        assertTrue(BlockStateFlags.FULL_OPAQUE in TestBlocks.OPAQUE2.flags)
        assertTrue(BlockStateFlags.FULL_OPAQUE in TestBlocks.OPAQUE3.flags)
    }

    fun `normal blocks are all not opaque`() {
        assertTrue(BlockStateFlags.FULL_OPAQUE !in TestBlocks.TEST1.flags)
        assertTrue(BlockStateFlags.FULL_OPAQUE !in TestBlocks.TEST1.flags)
        assertTrue(BlockStateFlags.FULL_OPAQUE !in TestBlocks.TEST1.flags)
    }

    fun `torch has luminance blocks are all not opaque`() {
        assertEquals(TestBlocks.TORCH14.states.default.luminance, 14)
    }
}
