/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util

import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.net.URL

object DesktopUtil {

    fun openURL(url: URL) {
        if (RunConfiguration.DISABLE_EROS) {
            Log.log(LogMessageType.GENERAL, LogLevels.INFO) { "Can not open url: $url" }
            return
        }
        try {
            JavaFXUtil.HOST_SERVICES.showDocument(url.toString())
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
    }

    fun openFile(path: String) {
        println("ToDo: Can not open file: $path")
    }
}
