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

package de.bixilon.minosoft

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.data.registries.fallback.FallbackRegistries
import de.bixilon.minosoft.data.registries.fallback.tags.FallbackTags
import de.bixilon.minosoft.protocol.packets.factory.PacketTypeRegistry
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.testng.annotations.BeforeSuite


internal object MinosoftSIT {

    @BeforeSuite
    fun setup() {
        Log.ASYNC_LOGGING = false
        disableGC()
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Setting up integration tests...." }
        initAssetsManager()
        setupPacketRegistry()
        loadVersionsJson()
        loadAssetsProperties()
        loadDefaultRegistries()
        loadPixlyzerData()
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Integration tests setup successfully!" }
    }


    @Deprecated("Not sure if that is needed")
    fun disableGC() {
        Thread {
            val references = IT.references
            // basically while (true)
            for (i in 0 until Int.MAX_VALUE) {
                Thread.sleep(100000L)
            }
            references.hashCode() // force keep reference to references
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
        FallbackTags.load()
        FallbackRegistries.load()
    }

    fun loadPixlyzerData() {
        val (version, registries) = ITUtil.loadPixlyzerData(IT.TEST_VERSION_NAME)
        IT.VERSION = version
        IT.REGISTRIES = registries
        IT.FALLBACK_TAGS = FallbackTags.map(registries)
    }
}
