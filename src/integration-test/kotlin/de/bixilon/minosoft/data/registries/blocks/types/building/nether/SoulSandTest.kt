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

import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet.Companion.plus
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.test.IT
import org.testng.AssertJUnit.assertEquals
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["block"])
class SoulSandTest {
    private val block by lazy { IT.REGISTRIES.block[SoulSand] ?: throw SkipException("") }

    fun `block state flags`() {
        val expected = BlockStateFlags.OUTLINE + BlockStateFlags.FULL_OUTLINE + BlockStateFlags.COLLISIONS + BlockStateFlags.FULL_OPAQUE + BlockStateFlags.VELOCITY

        assertEquals(expected, block.states.default.flags)
    }
}
