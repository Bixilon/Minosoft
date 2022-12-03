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

import de.bixilon.minosoft.util.SystemInformation
import oshi.hardware.GraphicsCard

class GPUSection(
    gpus: Array<GraphicsCard> = SystemInformation.HARDWARE_SYSTEM_INFO.graphicsCards.toTypedArray(),
) : ArrayCrashSection<GraphicsCard>("Connections", gpus) {

    override fun format(entry: GraphicsCard, builder: StringBuilder, intent: String) {
        builder.appendProperty(intent, "Name", entry.name)
        builder.appendProperty(intent, "Device id", entry.deviceId)
        builder.appendProperty(intent, "VRam", entry.vRam)
        builder.appendProperty(intent, "Vendor", entry.vendor)
        builder.appendProperty(intent, "Version info", entry.versionInfo)
    }
}
