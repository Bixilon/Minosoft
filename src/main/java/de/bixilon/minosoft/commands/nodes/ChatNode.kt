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

import de.bixilon.minosoft.commands.nodes.ConnectionNode.Companion.COMMAND_PREFIX
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.terminal.cli.CLI
import de.bixilon.minosoft.terminal.cli.CLI.CLI_PREFIX

class ChatNode(
    name: String,
    aliases: Set<String> = emptySet(),
    val allowCLI: Boolean = false,
) : ExecutableNode(name, aliases) {


    override fun execute(reader: CommandReader, stack: CommandStack) {
        reader.skipWhitespaces()
        val node = reader.readNode(stack)
        if (node == null) {
            stack.connection.util.sendChatMessage(reader.readRest() ?: return) // send normal chat message
        } else {
            node.execute(reader, stack)
        }
    }

    private fun CommandReader.readNode(stack: CommandStack): RootNode? {
        val peek = peek()
        val node = when (peek) {
            COMMAND_PREFIX.code -> stack.connection.commands
            CLI_PREFIX.code -> if (allowCLI) CLI.commands else null
            else -> null
        }

        if (node != null) {
            read() // remove prefix char
        }

        return node
    }

    override fun getSuggestions(reader: CommandReader, stack: CommandStack): Collection<Suggestion> {
        reader.skipWhitespaces()
        if (!reader.canPeek()) {
            val suggestions = mutableListOf(
                Suggestion(reader.pointer, "$COMMAND_PREFIX"),
            )
            if (allowCLI) {
                suggestions += Suggestion(reader.pointer, "$CLI_PREFIX")
            }
            return suggestions
        }
        val node = reader.readNode(stack)
        return node?.getSuggestions(reader, stack) ?: emptyList()
    }
}
