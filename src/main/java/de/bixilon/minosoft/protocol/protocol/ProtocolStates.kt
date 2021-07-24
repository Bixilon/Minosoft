/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

enum class ProtocolStates(val connected: Boolean, val protocolId: Int = -1) {
    CONNECTING(false),
    HANDSHAKING(true, 0),
    STATUS(true, 1),
    LOGIN(true, 2),
    PLAY(true, 3),
    DISCONNECTED(false),
    ;

    companion object : ValuesEnum<ProtocolStates> {
        override val VALUES: Array<ProtocolStates> = values()
        override val NAME_MAP: Map<String, ProtocolStates> = KUtil.getEnumValues(VALUES)
        val PROTOCOL_IDS = arrayOf(HANDSHAKING, STATUS, LOGIN, PLAY)
    }
}
