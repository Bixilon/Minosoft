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

package de.bixilon.minosoft.gui.rendering.system.opengl.vendor

import de.bixilon.minosoft.gui.rendering.system.base.GPUVendor
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

interface OpenGlVendor : GPUVendor {
    val usedVRAM: Long
        get() = -1L
    val availableVRAM: Long
        get() = -1L
    val maximumVRAM: Long
        get() = -1L


    companion object {

        fun of(vendor: String): OpenGlVendor = when {
            "nvidia" in vendor -> NvidiaOpenGlVendor
            "intel" in vendor -> IntelOpenGlVendor
            "amd" in vendor || "ati" in vendor -> AmdOpenGlVendor
            else -> {
                Log.log(LogMessageType.RENDERING, LogLevels.WARN) { "Can not detect gpu type from vendor: $vendor" }
                OtherOpenGlVendor
            }
        }
    }
}
