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

package de.bixilon.minosoft.data.registries.blocks

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.Block
import org.testng.annotations.Test

internal object StairsTest : BlockTest<Block>() {

    @Test
    private fun getOakStairs() {
        super.retrieveBlock(MinecraftBlocks.OAK_STAIRS)
    }

    @Test
    fun testLightPropertiesNorth() {
        block.withProperties(BlockProperties.FACING to Directions.NORTH).testLightProperties(0, true, true, false, booleanArrayOf(false, true, false, true, true, true))
    }

    @Test
    fun testLightPropertiesSouth() {
        block.withProperties(BlockProperties.FACING to Directions.SOUTH).testLightProperties(0, true, true, false, booleanArrayOf(false, true, true, false, true, true))
    }

    @Test
    fun testLightPropertiesWest() {
        block.withProperties(BlockProperties.FACING to Directions.WEST).testLightProperties(0, true, true, false, booleanArrayOf(false, true, true, true, false, true))
    }

    @Test
    fun testLightPropertiesEast() {
        block.withProperties(BlockProperties.FACING to Directions.EAST).testLightProperties(0, true, true, false, booleanArrayOf(false, true, true, true, true, false))
    }
}
