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

package de.bixilon.minosoft.commands.parser.minecraft.coordinate.rotation

import de.bixilon.minosoft.commands.parser.minecraft.coordinate.Coordinate
import de.bixilon.minosoft.commands.parser.minecraft.coordinate.CoordinateRelatives
import de.bixilon.minosoft.commands.parser.minecraft.coordinate.InvalidCoordinateError
import de.bixilon.minosoft.commands.util.CommandReader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


internal class RotationParserTest {

    @Test
    fun testNumeric() {
        val reader = CommandReader("12 34")
        assertEquals(RotationParser.parse(reader), Rotation(Coordinate(CoordinateRelatives.NONE, 12.0f), Coordinate(CoordinateRelatives.NONE, 34.0f)))
    }

    @Test
    fun testMixed() {
        val reader = CommandReader("~12 34")
        assertEquals(RotationParser.parse(reader), Rotation(Coordinate(CoordinateRelatives.TILDE, 12.0f), Coordinate(CoordinateRelatives.NONE, 34.0f)))
    }

    @Test
    fun testTilde() {
        val reader = CommandReader("~ ~")
        assertEquals(RotationParser.parse(reader), Rotation(Coordinate(CoordinateRelatives.TILDE, 0.0f), Coordinate(CoordinateRelatives.TILDE, 0.0f)))
    }

    @Test
    fun testInvalid() {
        val reader = CommandReader("hm")
        assertThrows<InvalidCoordinateError> { RotationParser.parse(reader) }
    }
}
