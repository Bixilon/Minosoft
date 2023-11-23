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

package de.bixilon.minosoft.gui.eros.util

import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.system.DesktopAPI
import java.io.File
import java.net.URL

class JavaFXSystemAPI : DesktopAPI() {

    override fun openURL(url: URL) {
        try {
            JavaFXUtil.HOST_SERVICES.showDocument(url.toString())
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
    }

    override fun openFile(file: File) {
        if (!file.exists()) {
            Log.log(LogMessageType.GENERAL, LogLevels.WARN) { "Can not open file $file: File does not exist!" }
            return
        }

        try {
            JavaFXUtil.HOST_SERVICES.showDocument(file.absolutePath)
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
    }
}
