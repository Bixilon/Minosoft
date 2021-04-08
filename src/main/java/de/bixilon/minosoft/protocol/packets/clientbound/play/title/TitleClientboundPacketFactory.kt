/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play.title

import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

object TitleClientboundPacketFactory {

    fun createPacket(buffer: PlayInByteBuffer): PlayClientboundPacket {
        return when (buffer.connection.mapping.titleActionsRegistry.get(buffer.readVarInt())!!) {
            TitleActions.SET_TITLE -> SetTitleClientboundPacket(buffer)
            TitleActions.SET_SUBTITLE -> SetSubTitleClientboundPacket(buffer)
            TitleActions.SET_ACTION_BAR -> SetActionBarTextClientboundPacket(buffer)
            TitleActions.SET_TIMES_AND_DISPLAY -> SetTimesAndDisplayClientboundPacket(buffer)
            TitleActions.HIDE -> HideTitleClientboundPacket()
            TitleActions.RESET -> ResetTitleClientboundPacket()
        }
    }

    enum class TitleActions {
        SET_TITLE,
        SET_SUBTITLE,
        SET_ACTION_BAR,
        SET_TIMES_AND_DISPLAY,
        HIDE,
        RESET,
        ;

        companion object : ValuesEnum<TitleActions> {
            override val VALUES = values()
            override val NAME_MAP = KUtil.getEnumValues(VALUES)
        }
    }
}
