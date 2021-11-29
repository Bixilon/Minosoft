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

package de.bixilon.minosoft.terminal

import com.google.common.base.StandardSystemProperty
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.OSUtil
import java.io.File

object RunConfiguration {
    var CONFIG_FILENAME = "minosoft.json" // Filename of minosoft's base configuration (located in AppData/Minosoft/config)

    var LOG_COLOR_MESSAGE = true // The message (after all prefixes) should be colored with ANSI color codes
    var LOG_COLOR_LEVEL = true // The level (e.g. [INFO]) should be colored
    var LOG_COLOR_TYPE = true // The type (e.g. [OTHER]) should be colored
    var LOG_RELATIVE_TIME = false // prefix all log messages with the relative start time in milliseconds instead of the formatted time

    var VERBOSE_ENTITY_META_DATA_LOGGING = false // if true, the entity meta data is getting serialized

    var DISABLE_EROS = false // if true, the whole javafx eros part is disabled
    var DISABLE_RENDERING = false // if true, rendering is disabled

    var AUTO_CONNECT_TO: String? = null

    var HOME_DIRECTORY: String = let {
        // Sets Config.homeDir to the correct folder per OS
        var homeDir: String = System.getProperty(StandardSystemProperty.USER_HOME.key())
        if (!homeDir.endsWith(File.separator)) {
            homeDir += "/"
        }
        homeDir += when (OSUtil.OS) {
            OSUtil.OSs.LINUX -> ".local/share/minosoft/"
            OSUtil.OSs.WINDOWS -> "AppData/Roaming/Minosoft/"
            OSUtil.OSs.MAC -> "Library/Application Support/Minosoft/"
            OSUtil.OSs.OTHER -> ".minosoft/"
        }
        val folder = File(homeDir)
        if (!folder.exists() && !folder.mkdirs()) {
            // failed creating folder
            throw RuntimeException("Could not create home folder ($homeDir)!")
        }
        folder.absolutePath + "/"
    }

    val TEMPORARY_FOLDER = System.getProperty("java.io.tmpdir", "$HOME_DIRECTORY/tmp/") + "/"

    var VERSION_STRING = "Minosoft ${StaticConfiguration.VERSION}"

    var SKIP_RENDERERS: List<ResourceLocation> = listOf()
}
