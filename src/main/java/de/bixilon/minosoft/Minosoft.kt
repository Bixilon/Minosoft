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

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool.async
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.ForcePooledRunnable
import de.bixilon.kutil.concurrent.worker.task.TaskWorker
import de.bixilon.kutil.concurrent.worker.task.WorkerTask
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.os.OSTypes
import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.kutil.reflection.ReflectionUtil.forceInit
import de.bixilon.kutil.shutdown.AbstractShutdownReason
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.kutil.time.TimeUtil.nanos
import de.bixilon.kutil.unit.UnitFormatter.formatNanos
import de.bixilon.minosoft.assets.IntegratedAssets
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.language.IntegratedLanguage
import de.bixilon.minosoft.data.text.formatting.FormattingCodes
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.eros.Eros
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.gui.eros.dialog.StartingDialog
import de.bixilon.minosoft.gui.eros.util.JavaFXInitializer
import de.bixilon.minosoft.main.BootTasks
import de.bixilon.minosoft.main.MinosoftBoot
import de.bixilon.minosoft.modding.event.events.FinishBootEvent
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.modding.loader.LoadingPhases
import de.bixilon.minosoft.modding.loader.ModLoader
import de.bixilon.minosoft.properties.MinosoftPropertiesLoader
import de.bixilon.minosoft.terminal.AutoConnect
import de.bixilon.minosoft.terminal.CommandLineArguments
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.system.DesktopAPI
import de.bixilon.minosoft.util.system.SystemUtil


object Minosoft {


    private fun preBoot(args: Array<String>) {
        val assets = SimpleLatch(1)
        DefaultThreadPool += ForcePooledRunnable { IntegratedAssets.DEFAULT.load(); assets.dec() }
        DefaultThreadPool += ForcePooledRunnable { Jackson.init(); MinosoftPropertiesLoader.init() }
        DefaultThreadPool += ForcePooledRunnable { KUtil.initBootClasses() }
        CommandLineArguments.parse(args)
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Starting minosoft..." }

        val latch = SimpleLatch(2)
        DefaultThreadPool += ForcePooledRunnable { ModLoader.initModLoading(); latch.dec() }
        assets.await()
        DefaultThreadPool += ForcePooledRunnable { MinosoftPropertiesLoader.load(); latch.dec() }

        KUtil.init()

        latch.await()
        ModLoader.load(LoadingPhases.PRE_BOOT)
        ModLoader.await(LoadingPhases.PRE_BOOT)

        if (PlatformInfo.OS == OSTypes.MAC) {
            checkMacOS()
        }
    }

    private fun boot() {
        val taskWorker = TaskWorker(errorHandler = { _, error -> error.printStackTrace(); error.crash() }, forcePool = true)

        MinosoftBoot.register(taskWorker)
        taskWorker += WorkerTask(identifier = BootTasks.LANGUAGE_FILES, dependencies = arrayOf(BootTasks.PROFILES), executor = this::loadLanguageFiles)

        if (!RunConfiguration.DISABLE_EROS) {
            javafx(taskWorker)
        }
        if (RunConfiguration.DISABLE_EROS && !RunConfiguration.DISABLE_RENDERING) {
            // eros is disabled, but rendering not, force initialize the desktop, otherwise eros will do so
            DefaultThreadPool += { SystemUtil.api = DesktopAPI() }
        }

        taskWorker.work(MinosoftBoot.LATCH)
        MinosoftBoot.LATCH.dec() // initial count
        MinosoftBoot.LATCH.await()
    }

    private fun postBoot() {
        if (ErosCrashReport.alreadyCrashed) return

        KUtil.initPlayClasses()
        GlobalEventMaster.fire(FinishBootEvent())
        DefaultThreadPool += { ModLoader.load(LoadingPhases.POST_BOOT) }
        if (RunConfiguration.DISABLE_EROS) {
            Log.log(LogMessageType.GENERAL, LogLevels.WARN) { "Eros is disabled, no gui will show up! Use the cli to connect to servers!" }
        }

        RunConfiguration.AUTO_CONNECT_TO?.let { AutoConnect.autoConnect(it) }
    }

    private fun javafx(taskWorker: TaskWorker) {
        taskWorker += WorkerTask(identifier = BootTasks.JAVAFX, executor = { JavaFXInitializer.start(); async(ThreadPool.HIGHER) { javafx.scene.text.Font.getDefault() } })

        taskWorker += WorkerTask(identifier = BootTasks.STARTUP_PROGRESS, executor = { StartingDialog(MinosoftBoot.LATCH).show() }, dependencies = arrayOf(BootTasks.LANGUAGE_FILES, BootTasks.JAVAFX))
        taskWorker += WorkerTask(identifier = BootTasks.EROS, dependencies = arrayOf(BootTasks.JAVAFX, BootTasks.PROFILES, BootTasks.MODS, BootTasks.VERSIONS, BootTasks.LANGUAGE_FILES), executor = { DefaultThreadPool += { Eros.preload() } })

        DefaultThreadPool += ForcePooledRunnable { Eros::class.java.forceInit() }
    }

    private fun initLog() {
        DefaultThreadPool += ForcePooledRunnable { Log.init() }
        DefaultThreadPool += ForcePooledRunnable { RunConfiguration }
        DefaultThreadPool += ForcePooledRunnable { FormattingCodes }
        DefaultThreadPool += ForcePooledRunnable { ChatColors }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val start = nanos()
        initLog()


        if (StaticConfiguration.DEBUG_MODE) {
            // Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Pre booting..." }
        }
        preBoot(args)

        Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Booting..." }
        boot()

        val delta = nanos() - start
        Log.log(LogMessageType.GENERAL, LogLevels.INFO) { "Minosoft boot sequence finished in ${delta.formatNanos()}!" }

        Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Post booting..." }
        postBoot()
    }

    private fun loadLanguageFiles(latch: AbstractLatch?) {
        ErosProfileManager.selected.general::language.observe(this, true) { IntegratedLanguage.load(it) }
    }

    private fun checkMacOS() {
        if (RunConfiguration.X_START_ON_FIRST_THREAD_SET && (!RunConfiguration.DISABLE_RENDERING || !RunConfiguration.DISABLE_EROS)) {
            Log.log(LogMessageType.GENERAL, LogLevels.WARN) { "You are using macOS. To use rendering you must not set the jvm argument §9-XstartOnFirstThread§r. Please remove it!" }
            ShutdownManager.shutdown(reason = AbstractShutdownReason.CRASH)
        }
    }
}
