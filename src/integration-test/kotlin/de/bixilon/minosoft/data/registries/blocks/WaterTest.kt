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

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.enums.inline.IntInlineSet
import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet
import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet.Companion.plus
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["block"])
class WaterTest : BlockTest<Block>() {

    init {
        WaterTest0 = this
    }

    override val type get() = MinecraftBlocks.WATER

    fun testLightProperties() {
        state.testLightProperties(0, true, false, true, booleanArrayOf(true, true, true, true, true, true))
    }

    fun `block state flags`() {
        val expected = IntInlineSet() + BlockStateFlags.FLUID + BlockStateFlags.OUTLINE + BlockStateFlags.TINTED + BlockStateFlags.RANDOM_TICKS + BlockStateFlags.CAVE_SURFACE

        assertEquals(expected, block.states.default.withProperties(FluidBlock.LEVEL to 1).flags)
    }
}

@Deprecated("")
var WaterTest0: WaterTest = unsafeNull()
