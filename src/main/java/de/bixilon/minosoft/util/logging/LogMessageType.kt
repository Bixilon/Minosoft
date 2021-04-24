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
package de.bixilon.minosoft.util.logging

import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

enum class LogMessageType(
    val color: RGBColor,
    val enabledDefault: Boolean = true,
    val error: Boolean = false,
) {
    GENERAL(ChatColors.WHITE),
    MOD_LOADING(ChatColors.GOLD),
    JAVAFX(ChatColors.DARK_GRAY),

    VERSION_LOADING(ChatColors.YELLOW),

    NETWORK_RESOLVING(ChatColors.DARK_GREEN),
    NETWORK_STATUS(ChatColors.DARK_GREEN, enabledDefault = false),
    NETWORK_PACKETS_IN(ChatColors.BLUE, enabledDefault = false),
    NETWORK_PACKETS_IN_ERROR(ChatColors.RED, error = true),
    NETWORK_PACKETS_OUT(ChatColors.DARK_AQUA, enabledDefault = false),

    RENDERING_GENERAL(ChatColors.GREEN),
    RENDERING_LOADING(ChatColors.GREEN),

    CHAT_IN(ChatColors.LIGHT_PURPLE),
    CHAT_OUT(ChatColors.LIGHT_PURPLE),

    OTHER_INFO(ChatColors.WHITE),
    OTHER_DEBUG(ChatColors.YELLOW),
    OTHER_ERROR(ChatColors.RED, error = true),
    OTHER_FATAL(ChatColors.DARK_RED, error = true),
    ;

    companion object : ValuesEnum<LogMessageType> {
        override val VALUES: Array<LogMessageType> = values()
        override val NAME_MAP: Map<String, LogMessageType> = KUtil.getEnumValues(VALUES)

        val DEFAULT_LOG_MESSAGE_TYPES = let {
            val ret: MutableSet<LogMessageType> = mutableSetOf()

            for (value in VALUES) {
                if (value.enabledDefault) {
                    ret += value
                }
            }
            ret.toSet()
        }
    }
}
