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

import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.assets.IntegratedAssets
import de.bixilon.minosoft.assets.meta.MinosoftMeta
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.data.registries.fallback.FallbackRegistries
import de.bixilon.minosoft.data.registries.fallback.tags.FallbackTags
import de.bixilon.minosoft.datafixer.DataFixer
import de.bixilon.minosoft.protocol.versions.VersionLoader
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.testng.annotations.BeforeSuite
import java.nio.file.Path


internal object MinosoftSIT {

    private fun setupEnv() {
        Log.ASYNC_LOGGING = false
        RunConfiguration.VERBOSE_LOGGING = true
        RunConfiguration.APPLICATION_NAME = "Minosoft it"

        val isCi = (System.getenv("GITHUB_ACTIONS") ?: System.getenv("TRAVIS") ?: System.getenv("CIRCLECI") ?: System.getenv("GITLAB_CI")) != null
        if (isCi) {
            RunConfiguration::HOME_DIRECTORY.forceSet(Path.of("./it"))
        }
        RunConfiguration::CONFIG_DIRECTORY.forceSet(Path.of(System.getProperty("java.io.tmpdir"), "minosoft").resolve("conf"))
        RunConfiguration.PROFILES_HOT_RELOADING = false
    }

    @BeforeSuite
    fun setup() {
        setupEnv()
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "This is java version ${System.getProperty("java.version")}" }
        KUtil.initBootClasses()
        KUtil.initPlayClasses()
        disableGC()
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Setting up integration tests...." }
        initAssetsManager()
        DataFixer.load()
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
        IntegratedAssets.DEFAULT.load()
        IntegratedAssets.OVERRIDE.load()
    }

    fun loadVersionsJson() {
        VersionLoader.load(SimpleLatch(0))
    }

    fun loadAssetsProperties() {
        AssetsVersionProperties.load(SimpleLatch(0))
    }

    fun loadDefaultRegistries() {
        FallbackTags.load()
        FallbackRegistries.load()
        MinosoftMeta.load()
    }

    fun loadPixlyzerData() {
        val (version, registries) = ITUtil.loadPixlyzerData(IT.TEST_VERSION_NAME)
        IT.VERSION = version
        IT.REGISTRIES = registries
        IT.FALLBACK_TAGS = FallbackTags.map(registries)
    }
}
