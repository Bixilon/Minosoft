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

import de.bixilon.minosoft.commands.parser.brigadier.string.StringParser
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.terminal.cli.CLI

class ChatNode(
    name: String,
    aliases: Set<String> = emptySet(),
    val allowCLI: Boolean = false,
) : ExecutableNode(name, aliases) {
    private val parser = StringParser(StringParser.StringModes.GREEDY)


    override fun execute(reader: CommandReader, stack: CommandStack) {
        reader.skipWhitespaces()
        val peek = reader.unsafePeek()
        val string = parser.parse(reader)
        val node = getNode(peek, stack)
        if (node != CLI.ROOT_NODE) {
            stack.connection.util.sendChatMessage(string)
        }
        node?.execute(CommandReader(string), stack)
    }

    private fun getNode(peek: Int, stack: CommandStack): RootNode? {
        return if (peek == '.'.code && allowCLI) CLI.ROOT_NODE else if (peek == '/'.code) stack.connection.rootNode else null
    }

    override fun getSuggestions(reader: CommandReader, stack: CommandStack): List<Any?> {
        reader.skipWhitespaces()
        val peek = reader.unsafePeek()
        val string = parser.parse(reader)
        return getNode(peek, stack)?.getSuggestions(CommandReader(string), stack) ?: emptyList()
    }
}
