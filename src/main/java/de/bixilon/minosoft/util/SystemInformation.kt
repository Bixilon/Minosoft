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

package de.bixilon.minosoft.util

import oshi.SystemInfo

object SystemInformation {
    val RUNTIME = Runtime.getRuntime()
    val SYSTEM_INFO = SystemInfo()
    val HARDWARE_SYSTEM_INFO = SYSTEM_INFO.hardware

    val SYSTEM_MEMORY_TEXT: String = UnitFormatter.formatBytes(HARDWARE_SYSTEM_INFO.memory.total)
    val OS_TEXT: String = "${System.getProperty("os.name")}: ${SYSTEM_INFO.operatingSystem.family} ${SYSTEM_INFO.operatingSystem.bitness}bit"

    val PROCESSOR_TEXT = " ${RUNTIME.availableProcessors()}x ${HARDWARE_SYSTEM_INFO.processor.processorIdentifier.name.replace("\\s{2,}".toRegex(), "")}"

    val MAX_MEMORY_TEXT: String = getFormattedMaxMemory()
    val PROCESSOR_SPEED = HARDWARE_SYSTEM_INFO.processor.maxFreq


    private fun getMaxMemory(): Long {
        return RUNTIME.maxMemory()
    }

    private fun getFormattedMaxMemory(): String {
        return UnitFormatter.formatBytes(getMaxMemory())
    }

}
