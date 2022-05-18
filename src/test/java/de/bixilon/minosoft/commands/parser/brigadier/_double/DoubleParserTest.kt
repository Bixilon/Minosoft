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

package de.bixilon.minosoft.commands.parser.brigadier._double

import de.bixilon.minosoft.commands.util.CommandReader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class DoubleParserTest {

    @Test
    fun readNormal() {
        val reader = CommandReader("1")
        val parser = DoubleParser()
        assertEquals(parser.parse(reader), 1.0)
    }

    @Test
    fun readNegative() {
        val reader = CommandReader("-1")
        val parser = DoubleParser()
        assertEquals(parser.parse(reader), -1.0)
    }

    @Test
    fun readTwice() {
        val reader = CommandReader("5,1")
        val parser = DoubleParser()
        assertEquals(parser.parse(reader), 5.0)
        reader.read()
        assertEquals(parser.parse(reader), 1.0)
    }

    @Test
    fun readDecimal() {
        val reader = CommandReader("1.1")
        val parser = DoubleParser()
        assertEquals(parser.parse(reader), 1.1)
    }

    @Test
    fun readNegativeDecimal() {
        val reader = CommandReader("-1.1")
        val parser = DoubleParser()
        assertEquals(parser.parse(reader), -1.1)
    }


    @Test
    fun readNoDecimal() {
        val reader = CommandReader("-1.0")
        val parser = DoubleParser()
        assertEquals(parser.parse(reader), -1.0)
    }

    @Test
    fun readInvalid() {
        val reader = CommandReader("abc")
        val parser = DoubleParser()
        assertThrows<DoubleParseError> { parser.parse(reader) }
    }

    @Test
    fun readBigNumber() {
        val reader = CommandReader("123456.7891")
        val parser = DoubleParser()
        assertEquals(parser.parse(reader), 123456.7891)
    }

    @Test
    fun readNegativeBigNumber() {
        val reader = CommandReader("-123456.7891")
        val parser = DoubleParser()
        assertEquals(parser.parse(reader), -123456.7891)
    }

    @Test
    fun checkMin() {
        val reader = CommandReader("5")
        val parser = DoubleParser(min = 0.0)
        assertEquals(parser.parse(reader), 5.0)
    }

    @Test
    fun checkMinError() {
        val reader = CommandReader("-2")
        val parser = DoubleParser(min = 0.0)
        assertThrows<DoubleOutOfRangeError> { parser.parse(reader) }
    }

    @Test
    fun checkMax() {
        val reader = CommandReader("3")
        val parser = DoubleParser(max = 5.0)
        assertEquals(parser.parse(reader), 3.0)
    }

    @Test
    fun checkMaxError() {
        val reader = CommandReader("10")
        val parser = DoubleParser(max = 8.0)
        assertThrows<DoubleOutOfRangeError> { parser.parse(reader) }
    }

    @Test
    fun checkMinMax() {
        val reader = CommandReader("4")
        val parser = DoubleParser(min = 3.0, max = 5.0)
        assertEquals(parser.parse(reader), 4.0)
    }

    @Test
    fun checkMinMaxError() {
        val reader = CommandReader("1")
        val parser = DoubleParser(min = 2.0, max = 8.0)
        assertThrows<DoubleOutOfRangeError> { parser.parse(reader) }
    }
}
