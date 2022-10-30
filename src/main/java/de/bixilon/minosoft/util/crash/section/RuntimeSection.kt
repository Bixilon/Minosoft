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

package de.bixilon.minosoft.util.crash.section

import de.bixilon.minosoft.terminal.CommandLineArguments
import de.bixilon.minosoft.terminal.RunConfiguration
import java.lang.management.ManagementFactory

class RuntimeSection : CrashSection(
    "Runtime details", arrayOf(
        "Start arguments" to CommandLineArguments.ARGUMENTS,
        "JVM flags" to ManagementFactory.getRuntimeMXBean().inputArguments,
        "Environment" to System.getenv(),
        "Home directory" to RunConfiguration.HOME_DIRECTORY,
    )
)
