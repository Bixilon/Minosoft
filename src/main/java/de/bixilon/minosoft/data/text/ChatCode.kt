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
package de.bixilon.minosoft.data.text

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.collections.CollectionUtil.extend
import de.bixilon.minosoft.data.text.RGBColor.Companion.asColor

interface ChatCode {
    companion object {
        val FORMATTING_CODES: Map<String, ChatCode> = ChatColors.NAME_MAP.extend(
            "dark_grey" to ChatColors.DARK_GRAY,
            "obfuscated" to PreChatFormattingCodes.OBFUSCATED,
            "bold" to PreChatFormattingCodes.BOLD,
            "strikethrough" to PreChatFormattingCodes.STRIKETHROUGH,
            "underlined" to PreChatFormattingCodes.UNDERLINED,
            "italic" to PreChatFormattingCodes.ITALIC,
            "reset" to PostChatFormattingCodes.RESET,
        )
        val FORMATTING_CODES_ID: List<ChatCode> = ChatColors.VALUES.toList().extend(
            PreChatFormattingCodes.OBFUSCATED,
            PreChatFormattingCodes.BOLD,
            PreChatFormattingCodes.STRIKETHROUGH,
            PreChatFormattingCodes.UNDERLINED,
            PreChatFormattingCodes.UNDERLINED,
            PreChatFormattingCodes.ITALIC,
            PostChatFormattingCodes.RESET,
        )

        operator fun get(name: String): ChatCode? {
            return FORMATTING_CODES[name]
        }

        operator fun get(id: Int): ChatCode? {
            return FORMATTING_CODES_ID.getOrNull(id)
        }

        operator fun get(char: Char): ChatCode? {
            return this[Character.digit(char, 16)]
        }

        operator fun get(chatCode: ChatCode): String? {
            val index = FORMATTING_CODES_ID.indexOf(chatCode)
            if (index == -1) {
                return null
            }
            return "%x".format(index)
        }

        fun String.toColor(): RGBColor? {
            if (this.startsWith("#")) {
                return this.asColor()
            }
            return FORMATTING_CODES[this].nullCast<RGBColor>()
        }
    }
}
