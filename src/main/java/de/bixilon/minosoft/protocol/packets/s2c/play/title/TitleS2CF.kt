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

package de.bixilon.minosoft.protocol.packets.s2c.play.title

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer

object TitleS2CF {

    fun createPacket(buffer: PlayInByteBuffer): PlayS2CPacket {
        return when (buffer.connection.registries.titleActionsRegistry[buffer.readVarInt()]!!) {
            TitleActions.SET_TITLE -> TitleSetS2CP(buffer)
            TitleActions.SET_SUBTITLE -> TitleSubtitleSetS2CP(buffer)
            TitleActions.SET_ACTION_BAR -> HotbarTextSetS2CP(buffer)
            TitleActions.SET_TIMES_AND_DISPLAY -> TitleTimesSetS2CP(buffer)
            TitleActions.HIDE -> TitleHideS2CP()
            TitleActions.RESET -> TitleResetS2CP()
        }
    }

    fun createClearTitlePacket(buffer: PlayInByteBuffer): PlayS2CPacket {
        val resetTimes = buffer.readBoolean()
        return if (resetTimes) {
            TitleResetS2CP()
        } else {
            TitleHideS2CP()
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
            override val NAME_MAP: Map<String, TitleActions> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
