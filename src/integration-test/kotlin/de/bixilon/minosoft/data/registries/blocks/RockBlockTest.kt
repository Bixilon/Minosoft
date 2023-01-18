/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kutil.cast.CastUtil
import de.bixilon.minosoft.data.registries.blocks.types.Block
import org.testng.annotations.Test

@Test(groups = ["block"])
class RockBlockTest : BlockTest<Block>() {

    init {
        StoneTestO = this
    }

    fun getStone() {
        super.retrieveBlock(MinecraftBlocks.STONE)
    }

    fun testLightProperties() {
        state.testLightProperties(0, false, false, true, booleanArrayOf(false, false, false, false, false, false))
    }
}

var StoneTestO: RockBlockTest = CastUtil.unsafeNull()
