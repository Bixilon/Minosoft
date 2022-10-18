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

package de.bixilon.minosoft

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager
import de.bixilon.minosoft.data.registries.DefaultRegistries
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.protocol.packets.factory.PacketTypeRegistry
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.testng.Assert
import org.testng.annotations.BeforeTest


internal object MinosoftSIT {

    @BeforeTest
    fun setup() {
        disableGC()
        initAssetsManager()
        setupPacketRegistry()
        loadVersionsJson()
        loadAssetsProperties()
        loadDefaultRegistries()
        `load 1_18_2 PixLyzer data`()
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Integration tests setup successfully!" }
    }


    fun disableGC() {
        Thread {
            val references = listOf(Minosoft, IT)
            // basically while (true)
            for (i in 0 until Int.MAX_VALUE) {
                Thread.sleep(100000L)
            }
            references.hashCode()
        }.start()
    }

    fun initAssetsManager() {
        Minosoft.MINOSOFT_ASSETS_MANAGER.load(CountUpAndDownLatch(0))
    }

    fun setupPacketRegistry() {
        PacketTypeRegistry.init(CountUpAndDownLatch(0))
    }

    fun loadVersionsJson() {
        Versions.load(CountUpAndDownLatch(0))
    }

    fun loadAssetsProperties() {
        AssetsVersionProperties.load(CountUpAndDownLatch(0))
    }

    fun loadDefaultRegistries() {
        DefaultRegistries.load(CountUpAndDownLatch(0))
    }

    fun `load 1_18_2 PixLyzer data`() {
        val version = Versions["1.18.2"]!!
        Assert.assertEquals(version.versionId, ProtocolVersions.V_1_18_2)
        IT.V_1_18_2 = version
        ResourcesProfileManager.currentLoadingPath = "dummy"
        val profile = ResourcesProfile()
        ResourcesProfileManager.currentLoadingPath = null
        version.load(profile, CountUpAndDownLatch(0))
    }
}
