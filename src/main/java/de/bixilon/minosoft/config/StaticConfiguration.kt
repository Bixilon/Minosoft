/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.config

import com.google.common.base.StandardSystemProperty
import de.bixilon.minosoft.util.OSUtil
import java.io.File

object StaticConfiguration {
    const val DEBUG_MODE = true // if true, additional checks will be made to validate data, ... Decreases performance
    const val BIOME_DEBUG_MODE = false // colors all biomes according to the biome hashCode
    const val DEBUG_SLOW_LOADING = false // if true, many Thread.sleep will be executed and the start will be delayed (by a lot)
    const val SHOW_LOG_MESSAGES_IN_CHAT = true // prints all console messages in the chat box
    const val REPLACE_SYSTEM_OUT_STREAMS = true

    @JvmField
    @Deprecated(message = "Not static, will be removed")
    var CONFIG_FILENAME = "minosoft.json" // Filename of minosoft's base configuration (located in AppData/Minosoft/config)

    @JvmField
    @Deprecated(message = "Not static, will be removed")
    var SKIP_MOJANG_AUTHENTICATION = false // disables all connections to mojang

    @Deprecated(message = "Not static, will be removed")
    var LOG_COLOR_MESSAGE = true // The message (after all prefixes) should be colored with ANSI color codes

    @Deprecated(message = "Not static, will be removed")
    var LOG_COLOR_LEVEL = true // The level (e.g. [INFO]) should be colored

    @Deprecated(message = "Not static, will be removed")
    var LOG_COLOR_TYPE = true // The type (e.g. [OTHER]) should be colored

    @JvmField
    @Deprecated(message = "Not static, will be removed")
    var LOG_RELATIVE_TIME = false // prefix all log messages with the relative start time in milliseconds instead of the formatted time

    @JvmField
    @Deprecated(message = "Not static, will be removed")
    var VERBOSE_ENTITY_META_DATA_LOGGING = false // if true, the entity meta data is getting serialized

    @JvmField
    @Deprecated(message = "Not static, will be removed")
    var HEADLESS_MODE = false // if true, no gui, rendering or whatever will be loaded or shown

    @JvmField
    @Deprecated(message = "Not static, will be removed")
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
            throw RuntimeException(String.format("Could not create home folder (%s)!", homeDir))
        }
        folder.absolutePath + "/"
    }
    val TEMPORARY_FOLDER = System.getProperty("java.io.tmpdir", "$HOME_DIRECTORY/tmp/") + "/"
}
