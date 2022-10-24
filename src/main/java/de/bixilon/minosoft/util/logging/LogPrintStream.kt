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
package de.bixilon.minosoft.util.logging

import java.io.PrintStream

class LogPrintStream(
    val type: LogMessageType = LogMessageType.OTHER,
    val level: LogLevels,
) : PrintStream(nullOutputStream()) {

    override fun print(string: String?) {
        if (string == null) {
            return
        }
        if (string.startsWith("SLF4J: ")) {
            return
        }
        if (string.startsWith("ERROR StatusLogger Log4j2")) {
            return
        }
        if (string.startsWith("Unknown element") && string.endsWith("):")) {
            return
        }
        if (Thread.currentThread().name == "JavaFX-Launcher") {
            return
        }
        Log.log(message = string, type = type, level = level)
    }

    override fun write(buf: ByteArray, off: Int, len: Int) {
        print(String(buf).substring(off, len)) // ToDo: Optimize
    }
}
