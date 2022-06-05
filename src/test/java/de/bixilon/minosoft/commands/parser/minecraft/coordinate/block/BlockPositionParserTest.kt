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

package de.bixilon.minosoft.commands.parser.minecraft.coordinate.block

import de.bixilon.minosoft.commands.parser.minecraft.coordinate.Coordinate
import de.bixilon.minosoft.commands.parser.minecraft.coordinate.CoordinateRelatives
import de.bixilon.minosoft.commands.util.CommandReader
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


internal class BlockPositionParserTest {

    @Test
    fun readNumber() {
        val reader = CommandReader("12 34 45")
        assertEquals(BlockPositionParser.parse(reader), BlockCoordinate(Coordinate(CoordinateRelatives.NONE, 12.0f), Coordinate(CoordinateRelatives.NONE, 34.0f), Coordinate(CoordinateRelatives.NONE, 45.0f)))
    }

    @Test
    fun readRelative() {
        val reader = CommandReader("~ ~ ~")
        assertEquals(BlockPositionParser.parse(reader), BlockCoordinate(Coordinate(CoordinateRelatives.TILDE, 0.0f), Coordinate(CoordinateRelatives.TILDE, 0.0f), Coordinate(CoordinateRelatives.TILDE, 0.0f)))
    }

    @Test
    fun readRelativeOffset() {
        val reader = CommandReader("~12 ~34 ~45")
        assertEquals(BlockPositionParser.parse(reader), BlockCoordinate(Coordinate(CoordinateRelatives.TILDE, 12.0f), Coordinate(CoordinateRelatives.TILDE, 34.0f), Coordinate(CoordinateRelatives.TILDE, 45.0f)))
    }

    @Test
    fun readDecimal() {
        val reader = CommandReader("12 34 45.56")
        assertEquals(BlockPositionParser.parse(reader), BlockCoordinate(Coordinate(CoordinateRelatives.NONE, 12.0f), Coordinate(CoordinateRelatives.NONE, 34.0f), Coordinate(CoordinateRelatives.NONE, 45.0f)))
    }
}
