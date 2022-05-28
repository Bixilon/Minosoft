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

package de.bixilon.minosoft.commands.nodes

import de.bixilon.minosoft.commands.errors.DeadEndError
import de.bixilon.minosoft.commands.errors.ExpectedArgumentError
import de.bixilon.minosoft.commands.errors.literal.ExpectedLiteralArgument
import de.bixilon.minosoft.commands.errors.literal.InvalidLiteralArgumentError
import de.bixilon.minosoft.commands.errors.literal.TrailingTextArgument
import de.bixilon.minosoft.commands.parser.brigadier.string.StringParser
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.util.CommandReader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


internal class ExecutionChildReadingTest {

    private fun createCommand(): CommandNode {
        return RootNode()
            .addChild(LiteralNode("0_literal"))
            .addChild(
                LiteralNode("1_literal")
                    .addChild(LiteralNode("1_literal_2"))
                    .addChild(LiteralNode("2_literal_2", executable = true))
            )
            .addChild(LiteralNode("2_literal", executable = true))
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
        val stack = CommandStack()
        assertDoesNotThrow { (createCommand().execute(CommandReader("1_execute test"), stack)) }
        assertNotNull(stack["1_execute"])
        assertEquals(stack["args"], "test")
    }

    @Test
    fun testValid2() {
        val stack = CommandStack()
        assertDoesNotThrow { (createCommand().execute(CommandReader("1_execute this is a really long valid greedy read string..."), stack)) }
        assertNotNull(stack["1_execute"])
        assertEquals(stack["args"], "this is a really long valid greedy read string...")
    }

    @Test
    fun testInvalidLiteralValid() {
        assertThrows<InvalidLiteralArgumentError> { (createCommand().execute(CommandReader("3_404_not_found"), CommandStack())) }
    }

    @Test
    fun testBlankStringArgument() {
        assertThrows<ExpectedArgumentError> { (createCommand().execute(CommandReader("1_execute "), CommandStack())) }
    }

    @Test
    fun testNoStringArgument() {
        assertThrows<ExpectedArgumentError> { (createCommand().execute(CommandReader("1_execute"), CommandStack())) }
    }

    @Test
    fun testDeadEnd() {
        assertThrows<DeadEndError> { (createCommand().execute(CommandReader("0_literal"), CommandStack())) }
    }

    @Test
    fun testTrailingData() {
        assertThrows<TrailingTextArgument> { (createCommand().execute(CommandReader("0_literal test"), CommandStack())) }
    }

    @Test
    fun testEmpty() {
        assertThrows<ExpectedLiteralArgument> { (createCommand().execute(CommandReader(""), CommandStack())) }
    }

    @Test
    fun testTrailingWhitespace() {
        assertDoesNotThrow { createCommand().execute(CommandReader("2_literal "), CommandStack()) }
    }

    @Test
    fun test2TrailingWhitespace() {
        assertDoesNotThrow { createCommand().execute(CommandReader("1_literal 2_literal_2 "), CommandStack()) }
    }

    @Test
    fun testEmptyRootNode() {
        assertThrows<TrailingTextArgument> { RootNode().execute(CommandReader(""), CommandStack()) }
    }

    @Test
    fun testTrailingTextEmptyRootNode() {
        assertThrows<TrailingTextArgument> { RootNode().execute(CommandReader("trailing"), CommandStack()) }
    }
}
