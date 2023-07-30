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

package de.bixilon.minosoft.terminal

import com.google.common.base.StandardSystemProperty
import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.os.OSTypes
import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.minosoft.modding.loader.parameters.ModParameters
import java.io.IOException
import java.nio.file.Path

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

    var HOME_DIRECTORY: Path = unsafeNull()
        private set
    var CONFIG_DIRECTORY: Path = unsafeNull()
        private set

    init {
        setDefaultHome()
    }


    val TEMPORARY_FOLDER: Path = Path.of(System.getProperty("java.io.tmpdir") ?: "$HOME_DIRECTORY/tmp/", "/minosoft/")

    val X_START_ON_FIRST_THREAD_SET = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_${ProcessHandle.current().pid()}") == "1"

    var APPLICATION_NAME = "Minosoft"

    var VERBOSE_LOGGING = false


    var IGNORE_YGGDRASIL = false

    var IGNORE_MODS = false

    var MOD_PARAMETERS = ModParameters()


    private fun setDefaultHome() {
        val user = System.getProperty(StandardSystemProperty.USER_HOME.key()) ?: throw IllegalStateException("Can not get user home!")

        val home = Path.of(user, when (PlatformInfo.OS) {
            OSTypes.LINUX -> ".local/share/minosoft/"
            OSTypes.WINDOWS -> "AppData/Roaming/Minosoft/"
            OSTypes.MAC -> "Library/Application Support/Minosoft/"
            else -> ".minosoft/"
        })
        setHome(home)

        val config = when (PlatformInfo.OS) {
            OSTypes.LINUX -> Path.of(user, ".config/minosoft")
            else -> home
        }
        setConfig(config)
    }

    fun setHome(path: Path) {
        val folder = path.toFile()

        if (!folder.exists() && !folder.mkdirs()) {
            throw IOException("Can not create home directory: $path")
        }
        HOME_DIRECTORY = path
    }

    fun setConfig(path: Path) {
        val folder = path.toFile()

        if (!folder.exists() && !folder.mkdirs()) {
            throw IOException("Can not create home directory: $path")
        }
        CONFIG_DIRECTORY = path
    }
}
