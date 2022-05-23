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

package de.bixilon.minosoft.commands.parser.minecraft.coordinate.angle

import de.bixilon.minosoft.commands.parser.minecraft.coordinate.CaretNotAllowedError
import de.bixilon.minosoft.commands.parser.minecraft.coordinate.Coordinate
import de.bixilon.minosoft.commands.parser.minecraft.coordinate.CoordinateRelatives
import de.bixilon.minosoft.commands.util.CommandReader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


internal class AngleParserTest {

    @Test
    fun readNumeric() {
        val reader = CommandReader("123")
        assertEquals(AngleParser.parse(reader), Coordinate(CoordinateRelatives.NONE, 123.0f))
    }

    @Test
    fun readTilde() {
        val reader = CommandReader("~123")
        assertEquals(AngleParser.parse(reader), Coordinate(CoordinateRelatives.TILDE, 123.0f))
    }

    @Test
    fun readNumber() {
        val reader = CommandReader("12")
        assertEquals(AngleParser.parse(reader), Coordinate(CoordinateRelatives.NONE, 12.0f))
    }

    @Test
    fun readRelative() {
        val reader = CommandReader("~")
        assertEquals(AngleParser.parse(reader), Coordinate(CoordinateRelatives.TILDE, 0.0f))
    }

    @Test
    fun readRelativeOffset() {
        val reader = CommandReader("~42")
        assertEquals(AngleParser.parse(reader), Coordinate(CoordinateRelatives.TILDE, 42.0f))
    }

    @Test
    fun readCaret() {
        val reader = CommandReader("^")
        assertThrows<CaretNotAllowedError> { AngleParser.parse(reader) }
    }

    @Test
    fun readDecimal() {
        val reader = CommandReader("12.34")
        assertEquals(AngleParser.parse(reader), Coordinate(CoordinateRelatives.NONE, 12.34f))
    }
}
