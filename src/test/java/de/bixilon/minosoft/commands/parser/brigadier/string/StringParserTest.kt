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

package de.bixilon.minosoft.commands.parser.brigadier.string

import de.bixilon.minosoft.commands.util.CommandReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StringParserTest {

    @Test
    fun readSingleString() {
        val reader = CommandReader("test")
        val parser = StringParser(StringParser.StringModes.SINGLE)
        assertEquals(parser.parse(reader), "test")
    }

    @Test
    fun read2SingleStrings() {
        val reader = CommandReader("test test2")
        val parser = StringParser(StringParser.StringModes.SINGLE)
        assertEquals(parser.parse(reader), "test")
        assertEquals(parser.parse(reader), "test2")
    }

    @Test
    fun readNotQuotedStrings() {
        val reader = CommandReader("test")
        val parser = StringParser(StringParser.StringModes.QUOTED)
        assertEquals(parser.parse(reader), "test")
    }

    @Test
    fun readQuotedStrings() {
        val reader = CommandReader("\"test\"")
        val parser = StringParser(StringParser.StringModes.QUOTED)
        assertEquals(parser.parse(reader), "test")
    }

    @Test
    fun readGreedyString() {
        val reader = CommandReader("this should all be in here")
        val parser = StringParser(StringParser.StringModes.GREEDY)
        assertEquals(parser.parse(reader), "this should all be in here")
    }

    @Test
    fun readQuoteGreedyString() {
        val reader = CommandReader("this should \"all\" be in here")
        val parser = StringParser(StringParser.StringModes.GREEDY)
        assertEquals(parser.parse(reader), "this should \"all\" be in here")
    }
}
