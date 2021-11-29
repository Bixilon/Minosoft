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
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.assets.JarAssetsManager
import de.bixilon.minosoft.data.assets.Resources
import de.bixilon.minosoft.data.language.LanguageManager.Companion.load
import de.bixilon.minosoft.data.language.MultiLanguageManager
import de.bixilon.minosoft.data.registries.DefaultRegistries
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.gui.eros.Eros
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.gui.eros.util.JavaFXInitializer
import de.bixilon.minosoft.modding.event.events.FinishInitializingEvent
import de.bixilon.minosoft.modding.event.events.connection.status.ServerStatusReceiveEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.modding.loading.ModLoader
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection
import de.bixilon.minosoft.protocol.protocol.LANServerListener
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.terminal.CLI
import de.bixilon.minosoft.terminal.CommandLineArguments
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.*
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.task.pool.ThreadPool
import de.bixilon.minosoft.util.task.worker.StartupTasks
import de.bixilon.minosoft.util.task.worker.TaskWorker
import de.bixilon.minosoft.util.task.worker.tasks.Task


object Minosoft {
    val MINOSOFT_ASSETS_MANAGER = JarAssetsManager(Minosoft::class.java, mutableSetOf("minosoft"))
    val LANGUAGE_MANAGER = MultiLanguageManager()
    val START_UP_LATCH = CountUpAndDownLatch(1)

    @Deprecated("Will be singleton interface")
    lateinit var config: Configuration

    var initialized: Boolean = false
        private set
    var configInitialized = false
        private set


    @JvmStatic
    fun main(args: Array<String>) {
        CommandLineArguments.parse(args)
        Util.initUtilClasses()

        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "Starting minosoft" }
        GitInfo.load()

        val taskWorker = TaskWorker(criticalErrorHandler = { _, exception -> exception.crash() })


        taskWorker += Task(identifier = StartupTasks.LOAD_VERSIONS, priority = ThreadPool.HIGH, executor = {
            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Loading versions..." }

            Versions.loadAvailableVersions(MINOSOFT_ASSETS_MANAGER.readLegacyJsonAsset(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "mapping/versions.json")))

            Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Versions loaded!" }
        })

        taskWorker += Task(identifier = StartupTasks.LOAD_CONFIG, priority = ThreadPool.HIGH, dependencies = arrayOf(StartupTasks.LOAD_VERSIONS), executor = {
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

            Util.forceClassInit(Eros::class.java)
        }


        taskWorker.work(START_UP_LATCH)

        START_UP_LATCH.dec() // remove initial count
        START_UP_LATCH.await()
        initialized = true
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "All startup tasks executed!" }

        GlobalEventMaster.fireEvent(FinishInitializingEvent())

        RunConfiguration.AUTO_CONNECT_TO?.let { autoConnect(it) }
    }

    private fun autoConnect(address: ServerAddress, version: Version, account: Account) {
        val connection = PlayConnection(
            address = address,
            account = account,
            version = version,
        )
        Log.log(LogMessageType.AUTO_CONNECT, LogLevels.INFO) { "Connecting to $address, with version $version using account $account..." }
        connection.connect()
    }

    private fun autoConnect(connectString: String) {
        // ToDo: Show those connections in eros
        val split = connectString.split(',')
        val address = split[0]
        val version = Versions.getVersionByName(split.getOrNull(1) ?: "automatic") ?: throw IllegalArgumentException("Auto connect: Version not found!")
        val account = Minosoft.config.config.account.entries[split.getOrNull(2)] ?: Minosoft.config.config.account.selected ?: throw RuntimeException("Auto connect: Account not found!")

        if (version == Versions.AUTOMATIC_VERSION) {
            Log.log(LogMessageType.AUTO_CONNECT, LogLevels.INFO) { "Pinging server to get version..." }
            val ping = StatusConnection(address)
            ping.ping()
            ping.registerEvent(CallbackEventInvoker.of<ServerStatusReceiveEvent> {
                autoConnect(ping.realAddress!!, ping.serverVersion ?: throw IllegalArgumentException("Could not determinate server's version!"), account)
            })
            return
        }

        autoConnect(DNSUtil.getServerAddress(address), version, account)
    }
}
