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

import de.bixilon.minosoft.commands.parser.minecraft.coordinate.CoordinateParserUtil.readCoordinate
import de.bixilon.minosoft.commands.util.CommandReader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class CoordinateParserUtilTest {

    @Test
    fun readNumericPositive() {
        val reader = CommandReader("5")
        assertEquals(Coordinate(CoordinateRelatives.NONE, 5.0f), reader.readCoordinate())
    }

    @Test
    fun readNumericNegative() {
        val reader = CommandReader("-5")
        assertEquals(Coordinate(CoordinateRelatives.NONE, -5.0f), reader.readCoordinate())
    }

    @Test
    fun readTilde() {
        val reader = CommandReader("~")
        assertEquals(Coordinate(CoordinateRelatives.TILDE, 0.0f), reader.readCoordinate())
    }

    @Test
    fun readTildePositiveOffset() {
        val reader = CommandReader("~1")
        assertEquals(Coordinate(CoordinateRelatives.TILDE, 1.0f), reader.readCoordinate())
    }

    @Test
    fun readTildeNegativeOffset() {
        val reader = CommandReader("~-1")
        assertEquals(Coordinate(CoordinateRelatives.TILDE, -1.0f), reader.readCoordinate())
    }

    @Test
    fun readCaret() {
        val reader = CommandReader("^")
        assertEquals(Coordinate(CoordinateRelatives.CARET, 0.0f), reader.readCoordinate())
    }

    @Test
    fun readCaretPositiveOffset() {
        val reader = CommandReader("^1")
        assertEquals(Coordinate(CoordinateRelatives.CARET, 1.0f), reader.readCoordinate())
    }

    @Test
    fun readCaretNegativeOffset() {
        val reader = CommandReader("^-1")
        assertEquals(Coordinate(CoordinateRelatives.CARET, -1.0f), reader.readCoordinate())
    }

    @Test
    fun readInvalidPrefix() {
        val reader = CommandReader("&12")
        assertThrows<InvalidCoordinateError> { reader.readCoordinate() }
    }

    @Test
    fun read2() {
        val reader = CommandReader("~ 12")
        assertEquals(Coordinate(CoordinateRelatives.TILDE, 0.0f), reader.readCoordinate())
        assertEquals(Coordinate(CoordinateRelatives.NONE, 12.0f), reader.readCoordinate())
    }
}
