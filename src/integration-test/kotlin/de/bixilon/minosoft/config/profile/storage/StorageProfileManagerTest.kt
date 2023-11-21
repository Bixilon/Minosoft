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

import de.bixilon.minosoft.config.profile.test.TestProfileManager
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import de.bixilon.minosoft.terminal.RunConfiguration
import org.testng.Assert.assertEquals
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

    fun `load dumped profiles`() {
        val profile = """{"version": "1", "key_old": 123}"""
        dump("Dumped", profile)

        val manager = TestProfileManager()
        assertEquals(manager["Dumped"], null)
        manager.load()
        val dumped = manager["Dumped"]!!
        assertEquals(dumped.key, 123)
    }
}
