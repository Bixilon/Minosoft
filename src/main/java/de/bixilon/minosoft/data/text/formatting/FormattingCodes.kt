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
package de.bixilon.minosoft.data.text.formatting

import de.bixilon.kutil.ansi.ANSI.formatting
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

enum class FormattingCodes(
    val char: Char,
    val ansi: String,
) {
    OBFUSCATED('k', formatting(5)),
    BOLD('l', formatting(1)),
    STRIKETHROUGH('m', formatting(9)),
    UNDERLINED('n', formatting(4)),
    ITALIC('o', formatting(3)),
    RESET('r', formatting(0)),
    ;

    val json: String = name.lowercase()

    override fun toString(): String {
        return ansi
    }

    companion object : ValuesEnum<FormattingCodes> {
        override val VALUES: Array<FormattingCodes> = values()
        override val NAME_MAP: Map<String, FormattingCodes> = EnumUtil.getEnumValues(VALUES)
        val CHARS = Int2ObjectOpenHashMap<FormattingCodes>()


        init {
            for (code in VALUES) {
                CHARS[code.char.code] = code
            }
        }

        operator fun get(name: Char): FormattingCodes? {
            return CHARS[name.code]
        }
    }
}
