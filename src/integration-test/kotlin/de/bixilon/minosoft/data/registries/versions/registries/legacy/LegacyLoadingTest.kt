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

package de.bixilon.minosoft.data.registries.versions.registries.legacy

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.state.PropertyBlockState
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.versions.registries.RegistryLoadingTest
import de.bixilon.minosoft.test.ITUtil
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["pixlyzer"], dependsOnGroups = ["version"], singleThreaded = false, threadPoolSize = 8, priority = Int.MAX_VALUE, timeOut = 15000L)
abstract class LegacyLoadingTest(version: String) : RegistryLoadingTest(version) {

    @Test(priority = 100000)
    open fun loadRegistries() {
        this._registries = ITUtil.loadPreFlatteningData(version)
    }

    fun dimension() {
        assertEquals(registries.dimension[0].identifier, minecraft("overworld"))
        assertEquals(registries.dimension[1].identifier, minecraft("the_end"))
        assertEquals(registries.dimension[-1].identifier, minecraft("the_nether"))
    }


    fun biomeId() {
        assertEquals(registries.biome[1].identifier, minecraft("plains"))
    }

    fun enchantmentId() {
        assertEquals(registries.enchantment[16].identifier, minecraft("sharpness"))
    }

    fun effectId() {
        assertEquals(registries.statusEffect[10].identifier, minecraft("regeneration"))
    }

    fun blockId() {
        assertNull(registries.block.getOrNull(0))
        assertEquals(registries.block[1 shl 4 or 0].identifier, minecraft("stone"))
        assertEquals(registries.block[1 shl 4 or 1].identifier, minecraft("granite"))
        assertEquals(registries.block[1 shl 4 or 3].identifier, minecraft("diorite"))
        assertEquals(registries.block[41 shl 4 or 0].identifier, minecraft("gold_block"))
        assertEquals(registries.block[166 shl 4 or 0].identifier, minecraft("barrier"))
    }

    fun blockStates() {
        assertNull(registries.blockState.getOrNull(0 shl 4 or 0))
        assertNull(registries.blockState.getOrNull(0 shl 4 or 1))

        assertEquals(registries.blockState.getOrNull(41 shl 4 or 0)?.block?.identifier, minecraft("gold_block"))
        assertEquals(registries.blockState.getOrNull(41 shl 4 or 12)?.block?.identifier, minecraft("gold_block"))

        assertEquals(registries.blockState.getOrNull(55 shl 4 or 0)?.block?.identifier, minecraft("redstone_wire"))
        assertEquals(registries.blockState.getOrNull(55 shl 4 or 1)?.block?.identifier, minecraft("redstone_wire"))
        assertEquals(registries.blockState.getOrNull(55 shl 4 or 2)?.nullCast<PropertyBlockState>()?.properties?.get(BlockProperties.REDSTONE_POWER), 2)
    }

    fun itemId() {
        assertEquals(registries.item[256].identifier, minecraft("iron_shovel"))
        assertEquals(registries.item[450].identifier, minecraft("shulker_shell"))
        assertEquals(registries.item[2256].identifier, minecraft("record_13"))
    }
}
