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

import de.bixilon.kutil.os.PlatformInfo
import de.bixilon.kutil.unit.UnitFormatter.formatBytes
import de.bixilon.minosoft.util.SystemInformation

class SystemSection : CrashSection(
    "System information", arrayOf(
        "Operating system" to SystemInformation.OS_TEXT,
        "Platform os" to PlatformInfo.OS,
        "Platform architecture" to PlatformInfo.ARCHITECTURE,
        "Java" to "${Runtime.version()} ${System.getProperty("sun.arch.data.model")}bit",
        "Memory" to SystemInformation.SYSTEM_MEMORY.formatBytes(),
        "Processor" to SystemInformation.PROCESSOR_TEXT,
    )
)
