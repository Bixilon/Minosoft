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

package de.bixilon.minosoft.commands.parser.brigadier.bool

import de.bixilon.minosoft.commands.errors.suggestion.NoSuggestionError
import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.commands.util.CommandReader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BooleanParserTest {

    @Test
    fun testTrue() {
        val reader = CommandReader("true")
        assertTrue(BooleanParser.parse(reader))
    }

    @Test
    fun testFalse() {
        val reader = CommandReader("false")
        assertFalse(BooleanParser.parse(reader))
    }

    @Test
    fun testCaseSensitivity() {
        val reader = CommandReader("True")
        assertThrows<BooleanParseError> { (BooleanParser.parse(reader)) }
    }

    @Test
    fun testEmpty() {
        val reader = CommandReader("")
        assertThrows<BooleanParseError> { (BooleanParser.parse(reader)) }
    }

    @Test
    fun testTrash() {
        val reader = CommandReader("this is trash")
        assertThrows<BooleanParseError> { (BooleanParser.parse(reader)) }
    }

    @Test
    fun testEmptySuggestion() {
        val reader = CommandReader("")
        assertEquals(BooleanParser.getSuggestions(reader).size, 2)
    }

    @Test
    fun testTrueSuggestion() {
        val reader = CommandReader("t")
        assertEquals(BooleanParser.getSuggestions(reader).getOrNull(0), Suggestion(0, "true"))
    }

    @Test
    fun testFullTrueSuggestion() {
        val reader = CommandReader("true")
        assertEquals(BooleanParser.getSuggestions(reader).getOrNull(0), Suggestion(0, "true"))
    }

    @Test
    fun testFalseSuggestion() {
        val reader = CommandReader("fa")
        assertEquals(BooleanParser.getSuggestions(reader).getOrNull(0), Suggestion(0, "false"))
    }

    @Test
    fun testFullFalseSuggestion() {
        val reader = CommandReader("false")
        assertEquals(BooleanParser.getSuggestions(reader).getOrNull(0), Suggestion(0, "false"))
    }

    @Test
    fun testNoSuggestion() {
        val reader = CommandReader("a")
        assertThrows<NoSuggestionError> { BooleanParser.getSuggestions(reader).size }
    }
}
