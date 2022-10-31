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

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.worker.task.TaskWorker
import de.bixilon.kutil.concurrent.worker.task.WorkerTask
import de.bixilon.kutil.file.watcher.FileWatcherService
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.os.OSTypes
import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.kutil.reflection.ReflectionUtil.forceInit
import de.bixilon.kutil.shutdown.AbstractShutdownReason
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.kutil.time.TimeUtil.nanos
import de.bixilon.kutil.unit.UnitFormatter.formatNanos
import de.bixilon.minosoft.assets.file.ResourcesAssetsUtil
import de.bixilon.minosoft.assets.properties.version.AssetsVersionProperties
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.language.LanguageUtil
import de.bixilon.minosoft.data.language.manager.MultiLanguageManager
import de.bixilon.minosoft.data.registries.DefaultRegistries
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.gui.eros.Eros
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.gui.eros.dialog.StartingDialog
import de.bixilon.minosoft.gui.eros.util.JavaFXInitializer
import de.bixilon.minosoft.main.BootTasks
import de.bixilon.minosoft.modding.event.events.FinishBootEvent
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.modding.loader.LoadingPhases
import de.bixilon.minosoft.modding.loader.ModLoader
import de.bixilon.minosoft.properties.MinosoftPropertiesLoader
import de.bixilon.minosoft.protocol.packets.factory.PacketTypeRegistry
import de.bixilon.minosoft.protocol.protocol.LANServerListener
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.terminal.AutoConnect
import de.bixilon.minosoft.terminal.CommandLineArguments
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.terminal.cli.CLI
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.KUtil.minosoft
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.yggdrasil.YggdrasilUtil


object Minosoft {
    val MINOSOFT_ASSETS_MANAGER = ResourcesAssetsUtil.create(Minosoft::class.java, canUnload = false)
    val OVERRIDE_ASSETS_MANAGER = ResourcesAssetsUtil.create(Minosoft::class.java, canUnload = false, prefix = "assets_override")
    val LANGUAGE_MANAGER = MultiLanguageManager()
    val BOOT_LATCH = CountUpAndDownLatch(1)

    @JvmStatic
    fun main(args: Array<String>) {
        val start = nanos()
        Log::class.java.forceInit()
        CommandLineArguments.parse(args)
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Starting minosoft..." }

        KUtil.initUtilClasses()
        KUtil.init()
        ModLoader.initModLoading()
        ModLoader.load(LoadingPhases.PRE_BOOT, CountUpAndDownLatch(0))
        ModLoader.await(LoadingPhases.PRE_BOOT)

        MINOSOFT_ASSETS_MANAGER.load(CountUpAndDownLatch(0))

        warnMacOS()
        MinosoftPropertiesLoader.load()

        val taskWorker = TaskWorker(criticalErrorHandler = { _, exception -> exception.crash() })

        taskWorker += WorkerTask(identifier = BootTasks.CLI, priority = ThreadPool.HIGH, executor = CLI::startThread)

        taskWorker += WorkerTask(identifier = BootTasks.PACKETS, priority = ThreadPool.HIGH, executor = PacketTypeRegistry::init)
        taskWorker += WorkerTask(identifier = BootTasks.VERSIONS, priority = ThreadPool.HIGH, dependencies = arrayOf(BootTasks.PACKETS), executor = Versions::load)
        taskWorker += WorkerTask(identifier = BootTasks.PROFILES, priority = ThreadPool.HIGH, dependencies = arrayOf(BootTasks.VERSIONS), executor = GlobalProfileManager::initialize)
        taskWorker += WorkerTask(identifier = BootTasks.FILE_WATCHER, priority = ThreadPool.HIGH, optional = true, executor = this::startFileWatcherService)

        taskWorker += WorkerTask(identifier = BootTasks.LANGUAGE_FILES, dependencies = arrayOf(BootTasks.PROFILES), executor = this::loadLanguageFiles)
        taskWorker += WorkerTask(identifier = BootTasks.ASSETS_PROPERTIES, dependencies = arrayOf(BootTasks.PROFILES), executor = AssetsVersionProperties::load)
        taskWorker += WorkerTask(identifier = BootTasks.DEFAULT_REGISTRIES, dependencies = arrayOf(BootTasks.PROFILES), executor = DefaultRegistries::load)


        taskWorker += WorkerTask(identifier = BootTasks.LAN_SERVERS, dependencies = arrayOf(BootTasks.PROFILES), executor = LANServerListener::listen)

        if (!RunConfiguration.DISABLE_EROS) {
            taskWorker += WorkerTask(identifier = BootTasks.JAVAFX, executor = { JavaFXInitializer.start() })
            DefaultThreadPool += { javafx.scene.text.Font::class.java.forceInit() }

            taskWorker += WorkerTask(identifier = BootTasks.STARTUP_PROGRESS, executor = { StartingDialog(BOOT_LATCH).show() }, dependencies = arrayOf(BootTasks.LANGUAGE_FILES, BootTasks.JAVAFX))

            Eros::class.java.forceInit()
        }
        taskWorker += WorkerTask(identifier = BootTasks.YGGDRASIL, executor = { YggdrasilUtil.load() })

        taskWorker += WorkerTask(identifier = BootTasks.ASSETS_OVERRIDE, executor = { OVERRIDE_ASSETS_MANAGER.load(it) })
        taskWorker += WorkerTask(identifier = BootTasks.MODS, executor = { ModLoader.load(LoadingPhases.BOOT, it) })


        taskWorker.work(BOOT_LATCH)

        BOOT_LATCH.dec() // remove initial count
        BOOT_LATCH.await()
        val end = nanos()
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Minosoft boot sequence finished in ${(end - start).formatNanos()}!" }
        GlobalEventMaster.fire(FinishBootEvent())
        DefaultThreadPool += { ModLoader.load(LoadingPhases.POST_BOOT, CountUpAndDownLatch(0)) }


        RunConfiguration.AUTO_CONNECT_TO?.let { AutoConnect.autoConnect(it) }
    }

    private fun startFileWatcherService(latch: CountUpAndDownLatch) {
        Log.log(LogMessageType.GENERAL, LogLevels.VERBOSE) { "Starting file watcher service..." }
        FileWatcherService.start()
        Log.log(LogMessageType.GENERAL, LogLevels.VERBOSE) { "File watcher service started!" }
    }

    private fun loadLanguageFiles(latch: CountUpAndDownLatch) {
        val language = ErosProfileManager.selected.general.language
        ErosProfileManager.selected.general::language.profileWatch(this, true) {
            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Loading language files (${language})" }
            LANGUAGE_MANAGER.translators[ProtocolDefinition.MINOSOFT_NAMESPACE] = LanguageUtil.load(it, null, MINOSOFT_ASSETS_MANAGER, minosoft("language/"))
            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Language files loaded!" }
        }
    }

    private fun warnMacOS() {
        if (PlatformInfo.OS == OSTypes.MAC && RunConfiguration.X_START_ON_FIRST_THREAD_SET && (!RunConfiguration.DISABLE_RENDERING || !RunConfiguration.DISABLE_EROS)) {
            Log.log(LogMessageType.GENERAL, LogLevels.WARN) { "You are using MacOS. To use rendering you must not set the jvm argument §9-XstartOnFirstThread§r. Please remove it!" }
            ShutdownManager.shutdown(reason = AbstractShutdownReason.CRASH)
        }
    }
}
