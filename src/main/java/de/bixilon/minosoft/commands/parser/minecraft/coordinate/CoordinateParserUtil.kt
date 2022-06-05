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

package de.bixilon.minosoft.commands.parser.minecraft.coordinate

import de.bixilon.minosoft.commands.parser.brigadier._float.FloatParser.Companion.readFloat
import de.bixilon.minosoft.commands.parser.brigadier._int.IntParser.Companion.readInt
import de.bixilon.minosoft.commands.util.CommandReader

object CoordinateParserUtil {

    fun CommandReader.readCoordinateOrNull(decimal: Boolean = true, caret: Boolean = true): Coordinate? {
        val peek = peek() ?: return null
        val type = when (peek) {
            '~'.code -> {
                read()
                CoordinateRelatives.TILDE
            }
            '^'.code -> {
                read()
                if (!caret) {
                    throw CaretNotAllowedError(this, pointer)
                }
                CoordinateRelatives.CARET
            }
            else -> CoordinateRelatives.NONE
        }

        val offset = if (peekWhitespaces() == 0) if (decimal) readFloat() else readInt()?.toFloat() else null
        if (offset == null && type == CoordinateRelatives.NONE) {
            return null
        }


        return Coordinate(type, offset ?: 0.0f)
    }


    fun CommandReader.readCoordinate(decimal: Boolean = true, caret: Boolean = true): Coordinate {
        readResult { readCoordinateOrNull(decimal, caret) }.let { return it.result ?: throw InvalidCoordinateError(this, it) }
    }
}
