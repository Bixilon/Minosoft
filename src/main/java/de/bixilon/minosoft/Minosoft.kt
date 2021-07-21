/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
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

import de.bixilon.minosoft.config.Configuration
import de.bixilon.minosoft.data.assets.JarAssetsManager
import de.bixilon.minosoft.data.assets.Resources
import de.bixilon.minosoft.data.language.LanguageManager.Companion.load
import de.bixilon.minosoft.data.language.MultiLanguageManager
import de.bixilon.minosoft.data.registries.DefaultRegistries
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.gui.eros.Eros
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.gui.eros.util.JavaFXInitializer
import de.bixilon.minosoft.modding.event.events.FinishInitializingEvent
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.modding.loading.ModLoader
import de.bixilon.minosoft.protocol.protocol.LANServerListener
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.terminal.CLI
import de.bixilon.minosoft.terminal.CommandLineArguments.parse
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.GitInfo
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.task.pool.ThreadPool
import de.bixilon.minosoft.util.task.worker.StartupTasks
import de.bixilon.minosoft.util.task.worker.TaskWorker
import de.bixilon.minosoft.util.task.worker.tasks.Task


object Minosoft {
    val MINOSOFT_ASSETS_MANAGER = JarAssetsManager(Minosoft::class.java, mutableSetOf("minosoft"))
    val MINECRAFT_FALLBACK_ASSETS_MANAGER = JarAssetsManager(Minosoft::class.java, mutableSetOf("minecraft"))
    val GLOBAL_EVENT_MASTER = GlobalEventMaster()
    val LANGUAGE_MANAGER = MultiLanguageManager()
    val START_UP_LATCH = CountUpAndDownLatch(1)
    lateinit var config: Configuration

    var initialized: Boolean = false
        private set
    var configInitialized = false
        private set


    @JvmStatic
    fun main(args: Array<String>) {
        parse(args)
        Util.initUtilClasses()

        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Starting minosoft" }
        GitInfo.load()

        val taskWorker = TaskWorker(criticalErrorHandler = { _, exception -> exception.crash() })


        taskWorker += Task(identifier = StartupTasks.LOAD_CONFIG, priority = ThreadPool.HIGH, executor = {
            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Loading config file..." }
            config = Configuration()
            configInitialized = true
            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Config file loaded!" }
        })

        taskWorker += Task(identifier = StartupTasks.LOAD_LANGUAGE_FILES, dependencies = arrayOf(StartupTasks.LOAD_CONFIG), executor = {
            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Loading language files (${config.config.general.language})" }
            LANGUAGE_MANAGER.translators[ProtocolDefinition.MINOSOFT_NAMESPACE] = load(config.config.general.language, null, ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "language/"))
            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Language files loaded!" }
        })

        taskWorker += Task(identifier = StartupTasks.LOAD_DEFAULT_REGISTRIES, dependencies = arrayOf(StartupTasks.LOAD_CONFIG), executor = {
            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Loading default registries..." }

            Versions.loadAvailableVersions(MINOSOFT_ASSETS_MANAGER.readLegacyJsonAsset(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "mapping/versions.json")))
            Resources.load()
            DefaultRegistries.load()

            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Default registries loaded!" }
        })

        taskWorker += Task(identifier = StartupTasks.LOAD_MODS, dependencies = arrayOf(StartupTasks.LOAD_CONFIG), executor = { progress: CountUpAndDownLatch -> ModLoader.loadMods(progress) })


        taskWorker += Task(identifier = StartupTasks.LISTEN_LAN_SERVERS, dependencies = arrayOf(StartupTasks.LOAD_CONFIG), executor = {
            if (!config.config.network.showLanServers) {
                return@Task
            }

            LANServerListener.listen()
        })

        taskWorker += Task(identifier = StartupTasks.INITIALIZE_CLI, executor = { CLI.initialize() })


        if (!RunConfiguration.DISABLE_EROS) {
            taskWorker += Task(identifier = StartupTasks.INITIALIZE_JAVAFX, executor = { JavaFXInitializer.start() })

            // ToDo: Show start up progress window

            Eros // Init class
        }


        taskWorker.work(START_UP_LATCH)

        START_UP_LATCH.dec() // remove initial count
        START_UP_LATCH.await()
        initialized = true
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "All startup tasks executed!" }

        GLOBAL_EVENT_MASTER.fireEvent(FinishInitializingEvent())
    }
}
