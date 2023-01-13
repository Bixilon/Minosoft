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

package de.bixilon.minosoft.data.registries.versions

import de.bixilon.minosoft.data.entities.entities.monster.Zombie
import de.bixilon.minosoft.data.registries.VersionRegistry
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.item.MinecraftItems
import de.bixilon.minosoft.protocol.protocol.VersionSupport
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.test.ITUtil
import org.testng.Assert
import org.testng.annotations.Test

@Test(groups = ["pixlyzer"], dependsOnGroups = ["version"], singleThreaded = false, threadPoolSize = 8, priority = Int.MAX_VALUE, timeOut = 10000L)
class PixLyzerLoadingTest {

    private fun VersionRegistry.test() {
        val registries = this.registries
        Assert.assertNotNull(registries.block[MinecraftBlocks.DIRT])
        Assert.assertNotNull(registries.item[MinecraftItems.COAL])
        Assert.assertNotNull(registries.entityType[Zombie])
    }

    fun `1_14_4`() {
        ITUtil.loadPixlyzerData("1.14.4").test()
    }

    fun `1_15`() {
        ITUtil.loadPixlyzerData("1.15").test()
    }

    fun `1_15_1`() {
        ITUtil.loadPixlyzerData("1.15.1").test()
    }

    fun `1_15_2`() {
        ITUtil.loadPixlyzerData("1.15.2").test()
    }

    fun `1_16`() {
        ITUtil.loadPixlyzerData("1.16").test()
    }

    fun `1_16_1`() {
        ITUtil.loadPixlyzerData("1.16.1").test()
    }

    fun `1_16_2`() {
        ITUtil.loadPixlyzerData("1.16.2").test()
    }

    fun `1_16_3`() {
        ITUtil.loadPixlyzerData("1.16.3").test()
    }

    fun `1_16_5`() {
        ITUtil.loadPixlyzerData("1.16.5").test()
    }

    fun `1_17`() {
        ITUtil.loadPixlyzerData("1.17").test()
    }

    fun `1_17_1`() {
        ITUtil.loadPixlyzerData("1.17.1").test()
    }

    fun `1_18_1`() {
        ITUtil.loadPixlyzerData("1.18.1").test()
    }

    fun `1_18_2`() {
        ITUtil.loadPixlyzerData("1.18.2").test()
    }

    fun `1_19`() {
        ITUtil.loadPixlyzerData("1.19").test()
    }

    fun `1_19_2`() {
        ITUtil.loadPixlyzerData("1.19.2").test()
    }

    fun `1_19_3`() {
        ITUtil.loadPixlyzerData("1.19.3").test()
    }

    fun latest() {
        val version = Versions.getById(VersionSupport.LATEST_VERSION)!!
        println("Latest version $version")
        val registries = ITUtil.loadPixlyzerData(version)
        VersionRegistry(version, registries).test()
    }
}
