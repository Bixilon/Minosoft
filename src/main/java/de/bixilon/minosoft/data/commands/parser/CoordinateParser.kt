/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.commands.parser

import de.bixilon.minosoft.data.commands.parser.exceptions.MixWorldAndLocalCoordinatesCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.number.DoubleCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.number.IntegerCommandParseException
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.buffers.ImprovedStringReader

abstract class CoordinateParser : CommandParser() {

    fun readCoordinate(stringReader: ImprovedStringReader, allowDecimal: Boolean): CoordinateNotations {
        var position = stringReader.readUntil(ProtocolDefinition.COMMAND_SEPARATOR).key
        val notation: CoordinateNotations = when {
            position.startsWith("~") -> {
                CoordinateNotations.TILDE
            }
            position.startsWith("^") -> {
                CoordinateNotations.CARET
            }
            else -> {
                CoordinateNotations.NONE
            }
        }
        if (notation != CoordinateNotations.NONE) {
            position = position.substring(1)
            if (position.isEmpty()) {
                return notation
            }
        }
        if (allowDecimal) {
            try {
                position.toDouble()
            } catch (exception: NumberFormatException) {
                throw DoubleCommandParseException(stringReader, position)
            }
        } else {
            try {
                position.toInt()
            } catch (exception: NumberFormatException) {
                throw IntegerCommandParseException(stringReader, position)
            }
        }

        return notation
    }

    fun readCoordinates(stringReader: ImprovedStringReader, allowDecimal: Boolean, count: Int) {
        var localCoordinates: Boolean? = null
        // read coordinates and check if somebody tries to mix world (~ or number) and local coordinates (^)
        for (i in 0 until count) {
            val notation = readCoordinate(stringReader, allowDecimal)
            if (localCoordinates == null) {
                localCoordinates = notation == CoordinateNotations.CARET
            } else {
                if ((notation == CoordinateNotations.CARET && !localCoordinates) || (notation != CoordinateNotations.CARET && localCoordinates)) {
                    throw MixWorldAndLocalCoordinatesCommandParseException(stringReader, "") // ToDo: print argument
                }
            }

        }
    }

    enum class CoordinateNotations {
        NONE,
        TILDE,
        CARET
    }
}
