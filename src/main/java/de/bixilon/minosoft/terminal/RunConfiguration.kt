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

package de.bixilon.minosoft.terminal

import com.google.common.base.StandardSystemProperty
import de.bixilon.kutil.file.FileUtil.slashPath
import de.bixilon.kutil.os.OSTypes
import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.modding.loader.parameters.ModParameters
import java.io.File
import java.io.IOException

object RunConfiguration {
    var LOG_COLOR_MESSAGE = true // The message (after all prefixes) should be colored with ANSI color codes
    var LOG_COLOR_LEVEL = true // The level (e.g. [INFO]) should be colored
    var LOG_COLOR_TYPE = true // The type (e.g. [OTHER]) should be colored
    var LOG_RELATIVE_TIME = false // prefix all log messages with the relative start time in milliseconds instead of the formatted time

    var DISABLE_EROS = false // if true, the whole javafx eros part is disabled
    var DISABLE_RENDERING = false // if true, rendering is disabled
    var DISABLE_CURSOR_CATCH = false
    var PROFILES_HOT_RELOADING = true

    var AUTO_CONNECT_TO: String? = null

    var HOME_DIRECTORY: String = let {
        // Sets Config.homeDir to the correct folder per OS
        var homeDir: String = System.getProperty(StandardSystemProperty.USER_HOME.key())
        if (!homeDir.endsWith(File.separator)) {
            homeDir += "/"
        }
        homeDir += when (PlatformInfo.OS) {
            OSTypes.LINUX -> ".local/share/minosoft/"
            OSTypes.WINDOWS -> "AppData/Roaming/Minosoft/"
            OSTypes.MAC -> "Library/Application Support/Minosoft/"
            else -> ".minosoft/"
        }
        val folder = File(homeDir)
        if (!folder.exists() && !folder.mkdirs()) {
            // failed creating folder
            throw IOException("Could not create home folder ($homeDir)!")
        }
        folder.slashPath + "/"
    }

    val TEMPORARY_FOLDER = System.getProperty("java.io.tmpdir", "$HOME_DIRECTORY/tmp/") + "/minosoft/"

    val X_START_ON_FIRST_THREAD_SET = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_${ProcessHandle.current().pid()}") == "1"

    var APPLICATION_NAME = "Minosoft"

    var SKIP_RENDERERS: List<ResourceLocation> = emptyList()

    var VERBOSE_LOGGING = false


    var IGNORE_YGGDRASIL = false

    var IGNORE_MODS = false

    var MOD_PARAMETERS = ModParameters()
}
