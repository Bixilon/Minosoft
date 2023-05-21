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

package de.bixilon.minosoft.protocol.protocol

import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_13W41B
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_19_4
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_20_PRE3

object VersionSupport {
    const val MINIMUM_VERSION = V_13W41B
    const val LATEST_VERSION = V_1_20_PRE3
    const val LATEST_RELEASE = V_1_19_4
}
