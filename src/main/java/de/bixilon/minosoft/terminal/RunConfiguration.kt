/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kutil.file.PathUtil.div
import de.bixilon.kutil.os.OSTypes
import de.bixilon.kutil.os.PlatformInfo
import java.nio.file.Path

object RunConfiguration {
    val X_START_ON_FIRST_THREAD_SET = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_${ProcessHandle.current().pid()}") == "1"
    var APPLICATION_NAME = "Minosoft"

    var home = run {
        val user = System.getProperty("user.home")?.let { Path.of(it) } ?: throw IllegalStateException("Can not get user home!")

        return@run user / when (PlatformInfo.OS) {
            OSTypes.LINUX -> ".local/share/minosoft"
            OSTypes.WINDOWS -> "AppData/Roaming/Minosoft"
            OSTypes.MAC -> "Library/Application Support/Minosoft"
            else -> ".minosoft"
        }
    }

    var temp = Path.of(System.getProperty("java.io.tmpdir")) ?: (home / "tmp" / "minosoft")
}
