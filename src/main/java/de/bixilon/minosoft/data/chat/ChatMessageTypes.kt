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

package de.bixilon.minosoft.data.chat

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum

enum class ChatMessageTypes(val position: ChatTextPositions = ChatTextPositions.CHAT) {
    CHAT_MESSAGE,
    SYSTEM_MESSAGE,
    GAME_MESSAGE,

    COMMAND_SAY,
    COMMAND_MSG,
    COMMAND_TEAM_MSG,
    COMMAND_EMOTE,
    COMMAND_TELLRAW,
    ;

    companion object : ValuesEnum<ChatMessageTypes> {
        override val VALUES: Array<ChatMessageTypes> = values()
        override val NAME_MAP: Map<String, ChatMessageTypes> = EnumUtil.getEnumValues(VALUES)
    }
}
