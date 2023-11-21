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

package de.bixilon.minosoft.config.profile.storage

import de.bixilon.minosoft.config.profile.storage.ProfileIOManagerTest.Companion.isSaveQueued
import de.bixilon.minosoft.config.profile.test.TestProfileManager
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import de.bixilon.minosoft.terminal.RunConfiguration
import org.testng.Assert.*
import org.testng.annotations.Test
import java.io.FileOutputStream

@Test(groups = ["profiles"])
class StorageProfileManagerTest {
    private val base by lazy { RunConfiguration.CONFIG_DIRECTORY.resolve("minosoft").resolve("test") }


    private fun dump(name: String, data: String) {
        val path = base.resolve("$name.json")
        path.parent.toFile().mkdirs()
        val stream = FileOutputStream(path.toFile())
        stream.write(data.encodeNetwork())
        stream.close()
    }

    fun `load unmigrated profile`() {
        val profile = """{"version": 2, "key": 123}"""
        dump("Dumped1", profile)

        val manager = TestProfileManager()
        assertEquals(manager["Dumped1"], null)
        manager.load()
        val dumped = manager["Dumped1"]!!
        assertFalse(dumped.isSaveQueued())
        assertEquals(dumped.key, 123)
        base.toFile().deleteRecursively()
    }

    fun `load migrated profile`() {
        val profile = """{"version": 1, "key_old": 123}"""
        dump("Dumped2", profile)

        val manager = TestProfileManager()
        assertEquals(manager["Dumped2"], null)
        manager.load()
        val dumped = manager["Dumped2"]!!
        assertTrue(dumped.isSaveQueued()) // profile was migrated
        assertEquals(dumped.key, 123)
        base.toFile().deleteRecursively()
    }

    fun `create and verify save is queued`() {
        val manager = TestProfileManager()
        val profile = manager.create("Dumped3")
        assertTrue(profile.isSaveQueued())
    }

    fun `queue save on change`() {
        val manager = TestProfileManager()
        val profile = manager.create("Dumped4")
        assertTrue(profile.isSaveQueued())
        assertFalse(profile.isSaveQueued()) // save flag really removed
        profile.key = 999
        assertTrue(profile.isSaveQueued())
    }

    // TODO: test reload, delete and selected queue
}
