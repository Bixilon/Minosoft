/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kutil.concurrent.worker.task.TaskWorker
import de.bixilon.kutil.environment.Environment
import de.bixilon.kutil.file.PathUtil.div
import de.bixilon.kutil.file.PathUtil.toPath
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.assets.IntegratedAssets
import de.bixilon.minosoft.config.profile.ProfileOptions
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.Andesite
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.Cobblestone
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.data.registries.fallback.tags.FallbackTags
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystemFactory
import de.bixilon.minosoft.gui.rendering.system.dummy.DummyRenderSystem
import de.bixilon.minosoft.gui.rendering.system.window.WindowFactory
import de.bixilon.minosoft.gui.rendering.system.window.dummy.DummyWindow
import de.bixilon.minosoft.main.BootTasks
import de.bixilon.minosoft.main.MinosoftBoot
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.logging.LogOptions
import org.testng.annotations.BeforeSuite
import java.nio.file.Path


internal object MinosoftSIT {

    private fun setupEnv() {
        LogOptions.async = false
        LogOptions.verbose = true
        RunConfiguration.APPLICATION_NAME = "Minosoft it"

        if (Environment.isInCI()) {
            RunConfiguration::home.forceSet(Path.of("./it"))
        }
        ProfileOptions.path = System.getProperty("java.io.tmpdir").toPath() / "minosoft" / "conf"
        ProfileOptions.hotReloading = false

        WindowFactory.factory = DummyWindow
        RenderSystemFactory.factory = DummyRenderSystem
    }

    @BeforeSuite
    fun setup() {
        setupEnv()
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "This is java version ${System.getProperty("java.version")}" }
        KUtil.initBootClasses()
        KUtil.initPlayClasses()
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Setting up integration tests...." }

        IntegratedAssets.DEFAULT.load()

        val worker = TaskWorker()
        MinosoftBoot.register(worker)
        worker -= BootTasks.PROFILES
        worker -= BootTasks.LAN_SERVERS
        worker -= BootTasks.MODS
        worker -= BootTasks.CLI
        worker.work(MinosoftBoot.LATCH)
        MinosoftBoot.LATCH.dec()
        MinosoftBoot.LATCH.await()

        loadPixlyzerData()
        IT.VERSION_LEGACY = Versions["1.12.2"]!!
        IT.REGISTRIES_LEGACY = ITUtil.loadPreFlatteningData(IT.VERSION_LEGACY)

        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Integration tests setup successfully!" }
    }

    fun loadPixlyzerData() {
        val (version, registries) = ITUtil.loadPixlyzerData(IT.TEST_VERSION_NAME)
        IT.VERSION = version
        IT.REGISTRIES = registries
        IT.FALLBACK_TAGS = FallbackTags.map(registries)

        IT::BLOCK_1.forceSet(registries.block[StoneBlock.Block]!!.states.default)
        IT::BLOCK_2.forceSet(registries.block[Cobblestone.Block]!!.states.default)
        IT::BLOCK_3.forceSet(registries.block[Andesite.Block]!!.states.default)
    }
}
