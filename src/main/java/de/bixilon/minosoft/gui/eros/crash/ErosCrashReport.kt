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

package de.bixilon.minosoft.gui.eros.crash

import de.bixilon.minosoft.ShutdownReasons
import de.bixilon.minosoft.gui.eros.JavaFXController
import de.bixilon.minosoft.gui.eros.util.JavaFXInitializer
import de.bixilon.minosoft.terminal.CommandLineArguments
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.*
import de.bixilon.minosoft.util.KUtil.toStackTrace
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Hyperlink
import javafx.scene.control.TextArea
import javafx.scene.text.TextFlow
import javafx.stage.Modality
import javafx.stage.Stage
import java.lang.management.ManagementFactory
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat


class ErosCrashReport : JavaFXController() {
    @FXML
    private lateinit var crashReportPathDescriptionFX: TextFlow

    @FXML
    private lateinit var crashReportPathFX: Hyperlink

    @FXML
    private lateinit var detailsFX: TextArea


    var crashReportPath: String? = null
        set(value) {
            field = value
            Platform.runLater {
                crashReportPathDescriptionFX.isVisible = value != null
                if (value != null) {
                    crashReportPathFX.text = value
                }
            }
        }


    var exception: Throwable? = null
        set(value) {
            field = value
            Platform.runLater { detailsFX.text = createCrashText(exception) }
        }


    fun exit() {
        ShutdownManager.shutdown(exception?.message, ShutdownReasons.CRITICAL_EXCEPTION)
    }

    fun hardCrash() {
        KUtil.hardCrash()
    }

    companion object {
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
            "Damn!",
            "Developing is hard.",
            "Please don't kill me for this",
            "Trying my best",
            "That happens when you develop while playing games!",
        )


        /**
         * Kills all connections, closes all windows, creates and saves a crash report
         * Special: Does not use any general functions/translations/..., because when a crash happens, you can't rely on anything.
         */
        fun Throwable?.crash() {
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

            Platform.runLater {
                val fxmlLoader = FXMLLoader(ErosCrashReport::class.java.getResource("/assets/minosoft/eros/crash/crash_screen.fxml"))
                val parent = fxmlLoader.load<Parent>()
                val stage = Stage()
                stage.initModality(Modality.APPLICATION_MODAL)
                stage.title = "Fatal Crash - Minosoft"
                stage.scene = Scene(parent)

                val crashReport = fxmlLoader.getController<ErosCrashReport>()
                crashReport.exception = this
                crashReport.crashReportPath = null

                // ToDo: Save crash report to file

                stage.setOnCloseRequest { ShutdownManager.shutdown(this?.message, ShutdownReasons.CRITICAL_EXCEPTION) }
                stage.show()
            }
        }

        private fun createCrashText(exception: Throwable?): String {
            val stack = """
----- Minosoft Crash Report -----
// ${CRASH_REPORT_COMMENTS.random()}

Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())} (${System.currentTimeMillis() / 1000L})

${exception?.toStackTrace() ?: ""}

-- Runtime Details --
    Start arguments: ${CommandLineArguments.ARGUMENTS}
    JVM Flags: ${ManagementFactory.getRuntimeMXBean().inputArguments}
    Home directory: ${RunConfiguration.HOME_DIRECTORY}
    Disable Eros: ${RunConfiguration.DISABLE_EROS}
    Disable Rendering: ${RunConfiguration.DISABLE_RENDERING}

-- System Details --
    Operating system: ${SystemInformation.OS_TEXT}
    Detected operating system: ${OSUtil.OS}
    Java version: Java: ${Runtime.version()} ${System.getProperty("sun.arch.data.model")}bit
    Memory: ${SystemInformation.SYSTEM_MEMORY_TEXT}
    CPU: ${SystemInformation.PROCESSOR_TEXT}
 
-- Git Info --
${GitInfo.formatForCrashReport()} 

TODO: Add more data
            """.trimIndent()

            val hash = Util.sha1(stack.toByteArray(StandardCharsets.UTF_8))

            return """
$stack

Crash checksum (SHA-1): $hash
            """.trimIndent()
        }
    }
}
