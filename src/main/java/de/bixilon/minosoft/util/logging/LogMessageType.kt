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
package de.bixilon.minosoft.util.logging

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor

enum class LogMessageType(
    val defaultColor: RGBColor,
    val colorMap: Map<LogLevels, RGBColor> = emptyMap(),
    val defaultLevel: LogLevels = LogLevels.INFO,
) {
    AUTO_CONNECT(ChatColors.WHITE),
    GENERAL(ChatColors.WHITE),
    MOD_LOADING(ChatColors.GOLD),
    JAVAFX(ChatColors.DARK_GRAY),

    LOADING(ChatColors.YELLOW),
    ASSETS(ChatColors.BLACK),

    AUTHENTICATION(ChatColors.BLACK),

    NETWORK(ChatColors.DARK_GREEN),
    NETWORK_IN(ChatColors.BLUE, defaultLevel = LogLevels.WARN, colorMap = mapOf(
        LogLevels.FATAL to ChatColors.DARK_RED,
        LogLevels.WARN to ChatColors.RED,
    )),
    NETWORK_OUT(ChatColors.DARK_AQUA, defaultLevel = LogLevels.WARN, colorMap = mapOf(
        LogLevels.FATAL to ChatColors.DARK_RED,
        LogLevels.WARN to ChatColors.RED,
    )),

    RENDERING(ChatColors.GREEN),
    AUDIO(ChatColors.DARK_PURPLE),

    CHAT_IN(ChatColors.LIGHT_PURPLE),
    CHAT_OUT(ChatColors.LIGHT_PURPLE),

    OTHER(ChatColors.WHITE, mapOf(
        LogLevels.FATAL to ChatColors.DARK_RED,
        LogLevels.WARN to ChatColors.RED,
        LogLevels.VERBOSE to ChatColors.YELLOW,
    )),

    PROFILES(ChatColors.AQUA),

    MODS(ChatColors.WHITE, mapOf(
        LogLevels.FATAL to ChatColors.DARK_RED,
        LogLevels.WARN to ChatColors.RED,
        LogLevels.VERBOSE to ChatColors.YELLOW,
    )),
    ;

    companion object : ValuesEnum<LogMessageType> {
        override val VALUES: Array<LogMessageType> = values()
        override val NAME_MAP: Map<String, LogMessageType> = EnumUtil.getEnumValues(VALUES)

        val DEFAULT_LOG_MAP: Map<LogMessageType, LogLevels> = let {
            val ret: MutableMap<LogMessageType, LogLevels> = mutableMapOf()

            for (value in VALUES) {
                ret[value] = value.defaultLevel
            }

            ret
        }
    }
}
