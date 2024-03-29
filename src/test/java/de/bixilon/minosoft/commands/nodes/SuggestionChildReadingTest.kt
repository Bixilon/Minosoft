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

package de.bixilon.minosoft.commands.nodes

import de.bixilon.minosoft.commands.errors.DeadEndError
import de.bixilon.minosoft.commands.errors.literal.InvalidLiteralArgumentError
import de.bixilon.minosoft.commands.errors.literal.TrailingTextError
import de.bixilon.minosoft.commands.errors.reader.ExpectedWhitespaceError
import de.bixilon.minosoft.commands.parser.brigadier.string.StringParseError
import de.bixilon.minosoft.commands.parser.brigadier.string.StringParser
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.commands.suggestion.util.SuggestionUtil
import de.bixilon.minosoft.commands.util.CommandReader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class SuggestionChildReadingTest {

    private fun createCommand(): CommandNode {
        return RootNode()
            .addChild(
                LiteralNode("1_literal")
                    .addChild(LiteralNode("1_literal_2"))
                    .addChild(LiteralNode("2_literal_2", executable = true))
            )
            .addChild(LiteralNode("2_literal"))
            .addChild(
                LiteralNode("3_literal", executable = true)
                    .addChild(ArgumentNode("arg", StringParser(StringParser.StringModes.SINGLE)))
            )
            .addChild(
                LiteralNode("1_execute")
                    .addChild(ArgumentNode("args", StringParser(StringParser.StringModes.GREEDY), executable = true))
            )
    }

    @Test
    fun testCreation() {
        assertTrue(createCommand() is RootNode)
    }

    @Test
    fun testValid() {
        assertEquals(createCommand().getSuggestions(CommandReader("1_execute test"), CommandStack()), emptyList())
    }

    @Test
    fun testValid2() {
        assertEquals(createCommand().getSuggestions(CommandReader("1_execute this is a really long valid greedy read string..."), CommandStack()), emptyList())
    }

    @Test
    fun testInvalidLiteralValid() {
        assertThrows<InvalidLiteralArgumentError> { (createCommand().getSuggestions(CommandReader("3_404_not_found"), CommandStack())) }
    }

    @Test
    fun testBlankStringArgument() {
        assertThrows<StringParseError> { (createCommand().getSuggestions(CommandReader("1_execute "), CommandStack())) }
    }

    @Test
    fun testNoStringArgument() {
        assertThrows<ExpectedWhitespaceError> { (createCommand().getSuggestions(CommandReader("1_execute"), CommandStack())) }
    }

    @Test
    fun testPrefixSuggestions() {
        assertEquals(createCommand().getSuggestions(CommandReader("1_"), CommandStack()), listOf(Suggestion(0, "1_literal"), Suggestion(0, "1_execute")))
    }

    @Test
    fun testEmptySuggestions() {
        assertEquals(createCommand().getSuggestions(CommandReader(""), CommandStack()), listOf(Suggestion(0, "1_literal"), Suggestion(0, "2_literal"), Suggestion(0, "3_literal"), Suggestion(0, "1_execute")))
    }

    @Test
    fun testWhitespaceSuggestions() {
        assertEquals(createCommand().getSuggestions(CommandReader(" "), CommandStack()), listOf(Suggestion(0, "1_literal"), Suggestion(0, "2_literal"), Suggestion(0, "3_literal"), Suggestion(0, "1_execute")))
    }

    @Test
    fun testTrailingData() {
        assertThrows<TrailingTextError> { (createCommand().getSuggestions(CommandReader("2_literal test"), CommandStack())) }
    }

    @Test
    fun test2InvalidLiteral() {
        assertThrows<InvalidLiteralArgumentError> { (createCommand().getSuggestions(CommandReader("1_literal test"), CommandStack())) }
    }

    @Test
    fun test2EmptyLevelSuggestions() {
        assertEquals(createCommand().getSuggestions(CommandReader("1_literal"), CommandStack()), listOf(Suggestion(9, "1_literal_2"), Suggestion(9, "2_literal_2")))
    }

    @Test
    fun test2TrailingData() {
        assertThrows<TrailingTextError> { (createCommand().getSuggestions(CommandReader("2_literal 1_literal_2 test"), CommandStack())) }
    }

    @Test
    fun checkDeadEnd() {
        assertThrows<DeadEndError> { createCommand().getSuggestions(CommandReader("2_literal"), CommandStack()) }
    }

    @Test
    fun testTrailingWhitespace() {
        assertThrows<DeadEndError> { createCommand().getSuggestions(CommandReader("2_literal "), CommandStack()) }
    }

    @Test
    fun test2TrailingWhitespace() {
        assertThrows<DeadEndError> { createCommand().getSuggestions(CommandReader("1_literal 1_literal_2 "), CommandStack()) }
    }

    @Test
    fun testEmptyNode() {
        assertThrows<DeadEndError> { RootNode().getSuggestions(CommandReader(""), CommandStack()) }
    }

    @Test
    fun testTrailingTextEmptyNode() {
        assertThrows<TrailingTextError> { RootNode().getSuggestions(CommandReader("trailing"), CommandStack()) }
    }

    @Test
    fun testEmptyWhitespaceSuggestions() {
        assertThrows<DeadEndError> { RootNode().getSuggestions(CommandReader(" "), CommandStack()) }
    }

    @Test
    fun testOptionalArguments() {
        assertDoesNotThrow { createCommand().getSuggestions(CommandReader("3_literal"), CommandStack()) }
    }

    @Test
    fun testStringOptionalArguments() {
        assertDoesNotThrow { createCommand().getSuggestions(CommandReader("3_literal string"), CommandStack()) }
    }

    @Test
    fun testNoSuggestionsErrors() {
        assertEquals(createCommand().getSuggestions(CommandReader("1_literal 2_literal_2"), CommandStack()), emptyList())
    }

    @Test
    fun applySuggestion() {
        val input = "1_li"
        val suggestion = createCommand().getSuggestions(CommandReader(input), CommandStack()).first() // 1_literal
        assertEquals(SuggestionUtil.apply(input, suggestion), "1_literal")
    }

    @Test
    fun applySuggestionAndWhitespace() {
        val input = "1_literal"
        val suggestion = createCommand().getSuggestions(CommandReader(input), CommandStack()).first() // 1_literal_2
        assertEquals(SuggestionUtil.apply(input, suggestion), "1_literal 1_literal_2")
    }

    // TODO: test overlapping apply (e.g. weather rain)
}
