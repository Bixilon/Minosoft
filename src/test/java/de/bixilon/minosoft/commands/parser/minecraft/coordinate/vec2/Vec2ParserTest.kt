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

package de.bixilon.minosoft.commands.parser.minecraft.coordinate.vec2

import de.bixilon.minosoft.commands.parser.minecraft.coordinate.Coordinate
import de.bixilon.minosoft.commands.parser.minecraft.coordinate.CoordinateRelatives
import de.bixilon.minosoft.commands.parser.minecraft.coordinate.InvalidCoordinateError
import de.bixilon.minosoft.commands.util.CommandReader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


internal class Vec2ParserTest {

    @Test
    fun readNumeric() {
        val reader = CommandReader("5 5")
        assertEquals(Vec2Coordinate(Coordinate(CoordinateRelatives.NONE, 5.0f), Coordinate(CoordinateRelatives.NONE, 5.0f)), Vec2Parser.parse(reader))
    }

    @Test
    fun readTilde() {
        val reader = CommandReader("~ ~")
        assertEquals(Vec2Coordinate(Coordinate(CoordinateRelatives.TILDE, 0.0f), Coordinate(CoordinateRelatives.TILDE, 0.0f)), Vec2Parser.parse(reader))
    }

    @Test
    fun readCaret() {
        val reader = CommandReader("^ ^")
        assertEquals(Vec2Coordinate(Coordinate(CoordinateRelatives.CARET, 0.0f), Coordinate(CoordinateRelatives.CARET, 0.0f)), Vec2Parser.parse(reader))
    }

    @Test
    fun readNumericTilde() {
        val reader = CommandReader("5 ~")
        assertEquals(Vec2Coordinate(Coordinate(CoordinateRelatives.NONE, 5.0f), Coordinate(CoordinateRelatives.TILDE, 0.0f)), Vec2Parser.parse(reader))
    }

    @Test
    fun readTildeCaret() {
        val reader = CommandReader("~4 ^-9")
        assertEquals(Vec2Coordinate(Coordinate(CoordinateRelatives.TILDE, 4.0f), Coordinate(CoordinateRelatives.CARET, -9.0f)), Vec2Parser.parse(reader))
    }

    @Test
    fun readOnlyOne() {
        val reader = CommandReader("~4")
        assertThrows<InvalidCoordinateError> { Vec2Parser.parse(reader) }
    }
}
