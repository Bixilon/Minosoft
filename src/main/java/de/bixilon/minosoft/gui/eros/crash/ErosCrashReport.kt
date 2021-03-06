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

package de.bixilon.minosoft.gui.eros.crash

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedSet
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.exception.ExceptionUtil.toStackTrace
import de.bixilon.kutil.exception.ExceptionUtil.tryCatch
import de.bixilon.kutil.file.FileUtil.slashPath
import de.bixilon.kutil.file.watcher.FileWatcherService
import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.kutil.unit.UnitFormatter.formatBytes
import de.bixilon.kutil.unsafe.UnsafeUtil
import de.bixilon.minosoft.ShutdownReasons
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.util.JavaFXInitializer
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.terminal.CommandLineArguments
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.GitInfo
import de.bixilon.minosoft.util.SystemInformation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Hyperlink
import javafx.scene.control.TextArea
import javafx.scene.text.TextFlow
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import java.io.File
import java.io.FileOutputStream
import java.lang.management.ManagementFactory
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat


class ErosCrashReport : JavaFXWindowController() {
    @FXML private lateinit var crashReportPathDescriptionFX: TextFlow
    @FXML private lateinit var crashReportPathFX: Hyperlink
    @FXML private lateinit var detailsFX: TextArea


    var crashReportPath: String? = null
        set(value) {
            field = value
            JavaFXUtil.runLater {
                crashReportPathDescriptionFX.isVisible = value != null
                if (value != null) {
                    crashReportPathFX.text = value
                }
            }
        }

    var exception: Throwable? = null

    var details: String? = null
        set(value) {
            field = value
            JavaFXUtil.runLater { detailsFX.text = value }
        }

    fun exit() {
        ShutdownManager.shutdown(exception?.message, ShutdownReasons.CRITICAL_EXCEPTION)
    }

    fun hardCrash() {
        UnsafeUtil.hardCrash()
    }

    companion object {
        var alreadyCrashed = false
            private set
        private val CRASH_REPORT_COMMENTS = listOf(
            "Let's blame Bixilon for this",
            "But it worked once",
            "It works on my computer",
            "Not a bug, it's a feature",
            "My bad",
            "Whoops",
            "Don't try to crash this!",
            "Makes not sense!",
            "Let's hack the game",
            "You're evil",
            "Maybe in another life.",
            "This sucks",
            "Chill ur life",
            "Chill your life",
            "Chill your base",
            "Damn!",
            "Developing is hard.",
            "Please don't kill me for this",
            "Trying my best",
            "That happens when you develop while playing games!",
            "Written while driving in a FlixBus",
            "Coded while traveling in the ICE 272 towards Hamburg-Altona",
            "Sorry, the ICE 693 drive towards Munich was really long",
            "Coded while playing bedwars",
            "I am #1 in bedwars swordless",
            "Der AB kam vor der CD",
            "You can't do this",
            "Sing me a happy song!",
            "This message should not be visible...",
            "lmfao",
            "Your fault",
            "Technoblade never dies", // In memorial to technoblade. RIP Technoblade 30.6.2022
        )


        /**
         * Kills all connections, closes all windows, creates and saves a crash report
         * Special: Does not use any general functions/translations/..., because when a crash happens, you can't rely on anything.
         */
        fun Throwable?.crash(hideException: Boolean = false) {
            if (alreadyCrashed) {
                return
            }
            alreadyCrashed = true
            val details = try {
                createCrashText(if (hideException) null else this)
            } catch (error: Throwable) {
                error.toStackTrace()
            }

            // Kill some stuff
            tryCatch(executor = { DefaultThreadPool.shutdownNow() })
            tryCatch(executor = { FileWatcherService.stop() })
            tryCatch(executor = {
                for (window in Window.getWindows().toSynchronizedSet()) {
                    JavaFXUtil.runLater { window.hide() }
                }
            })
            tryCatch(executor = {
                for (connection in PlayConnection.ACTIVE_CONNECTIONS.toSynchronizedSet()) {
                    connection.network.disconnect()
                }
            })


            var crashReportPath: String?
            try {
                val crashReportFolder = File(RunConfiguration.HOME_DIRECTORY + "crash-reports")
                crashReportFolder.mkdirs()

                crashReportPath = "${crashReportFolder.slashPath}/crash-${SimpleDateFormat("yyyy-MM-dd-HH.mm.ss").format(TimeUtil.millis)}.txt"

                val stream = FileOutputStream(crashReportPath)

                stream.write(details.toByteArray(StandardCharsets.UTF_8))
                stream.close()
            } catch (exception: Throwable) {
                exception.printStackTrace()
                crashReportPath = null
            }

            if (RunConfiguration.DISABLE_EROS) {
                ShutdownManager.shutdown(this?.message, ShutdownReasons.CRITICAL_EXCEPTION)
                return
            }

            if (!JavaFXInitializer.initializing && !JavaFXInitializer.initialized) {
                try {
                    JavaFXInitializer.start()
                } catch (exception: Throwable) {
                    Log.log(LogMessageType.JAVAFX, LogLevels.WARN) { "Can not show crash report screen!" }
                    exception.printStackTrace()
                    return
                }
            }

            JavaFXInitializer.await()

            JavaFXUtil.runLater {
                val fxmlLoader = FXMLLoader(ErosCrashReport::class.java.getResource("/assets/minosoft/eros/crash/crash_screen.fxml"))
                val parent = fxmlLoader.load<Parent>()
                val stage = Stage()
                stage.initModality(Modality.APPLICATION_MODAL)
                stage.title = "Fatal Crash - Minosoft"
                stage.scene = Scene(parent)
                stage.icons.setAll(JavaFXUtil.MINOSOFT_LOGO)
                ErosCrashReport::class.java.getResource("/assets/minosoft/eros/style.css")?.toExternalForm()?.let { stage.scene.stylesheets.add(it) }

                val crashReport = fxmlLoader.getController<ErosCrashReport>()
                crashReport.exception = this
                crashReport.details = details
                crashReport.crashReportPath = crashReportPath
                crashReport.stage = stage

                stage.setOnCloseRequest { ShutdownManager.shutdown(this?.message, ShutdownReasons.CRITICAL_EXCEPTION) }
                stage.show()
            }
        }

        private fun createCrashText(exception: Throwable?): String {
            var connections = """
-- Connections --"""

            fun addConnection(connection: PlayConnection) {
                connections += """
    #${connection.connectionId}:
        Version: ${connection.version}
        Account: ${connection.account.username}
        Address: ${connection.address}
        Brand: ${connection.serverInfo.brand}
        Events: ${connection.size}
        State: ${connection.state}
        Connected: ${connection.network.connected}
        Protocol state: ${connection.network.state}
        Compression threshold: ${connection.network.compressionThreshold}
        Encrypted: ${connection.network.encrypted}
        Was connected: ${connection.wasConnected}
        Rendering: ${connection.rendering != null}
        Error: ${connection.error}
""".removeSuffix("\n")
            }

            for (connection in PlayConnection.ACTIVE_CONNECTIONS.toSynchronizedSet()) {
                addConnection(connection)
            }

            for (connection in PlayConnection.ERRORED_CONNECTIONS.toSynchronizedSet()) {
                addConnection(connection)
            }

            val stackTraceText = if (exception == null) "" else """
-- Stacktrace --
${exception.toStackTrace()}"""

            return """
----- Minosoft Crash Report -----
// ${CRASH_REPORT_COMMENTS.random()}

-- General Information --
    Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(TimeUtil.millis)} (${TimeUtil.millis / 1000L})
    Crash thread: ${Thread.currentThread().name}
$stackTraceText
$connections

-- Runtime Details --
    Start arguments: ${CommandLineArguments.ARGUMENTS}
    JVM flags: ${ManagementFactory.getRuntimeMXBean().inputArguments}
    Home directory: ${RunConfiguration.HOME_DIRECTORY}
    Disable Eros: ${RunConfiguration.DISABLE_EROS}
    Disable rendering: ${RunConfiguration.DISABLE_RENDERING}

-- System Details --
    Operating system: ${SystemInformation.OS_TEXT}
    Detected operating system: ${PlatformInfo.OS}
    Detected architecture: ${PlatformInfo.ARCHITECTURE}
    Java version: ${Runtime.version()} ${System.getProperty("sun.arch.data.model")}bit
    Memory: ${SystemInformation.SYSTEM_MEMORY.formatBytes()}
    CPU: ${SystemInformation.PROCESSOR_TEXT}
 
-- Git Info --
${GitInfo.formatForCrashReport()}
""".removeSuffix("\n")
        }
    }
}
