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

package de.bixilon.minosoft.data.registries.versions.registries

import de.bixilon.minosoft.data.entities.entities.item.ItemEntity
import de.bixilon.minosoft.data.entities.entities.monster.Zombie
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.item.MinecraftItems
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_19_3
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotNull
import org.testng.SkipException
import org.testng.annotations.Test

@Test(groups = ["pixlyzer"], dependsOnGroups = ["version"], priority = Int.MAX_VALUE, timeOut = 15000L)
abstract class RegistryLoadingTest(val versionName: String) {
    protected var _registries: Registries? = null
    val registries: Registries get() = _registries ?: throw SkipException("")
    protected var _version: Version? = null
    val version: Version get() = _version ?: throw SkipException("")


    @Test(priority = 10000)
    open fun loadVersion() {
        this._version = Versions[versionName]
    }


    fun testItemSimple() {
        assertNotNull(registries.item[MinecraftItems.COAL])
    }

    fun testBlockSimple() {
        assertNotNull(registries.block[MinecraftBlocks.DIRT])
    }

    fun entities() {
        assertEquals(registries.entityType[RemotePlayerEntity]?.height, 1.8f)
        assertEquals(registries.entityType[Zombie]?.height, 1.95f)
        assertEquals(registries.entityType[ItemEntity]?.height, 0.25f)
    }

    fun biome() {
        if (version > V_1_19_3) return // biomes are datapack only in those versions -> empty registry

        assertNotNull(registries.biome[minecraft("plains")]?.identifier)
    }
}
