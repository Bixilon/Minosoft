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

import de.bixilon.minosoft.commands.errors.literal.ExpectedLiteralArgument
import de.bixilon.minosoft.commands.errors.literal.InvalidLiteralArgumentError
import de.bixilon.minosoft.commands.nodes.builder.CommandNodeBuilder
import de.bixilon.minosoft.commands.stack.CommandExecutor
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.suggestion.ArraySuggestion
import de.bixilon.minosoft.commands.suggestion.types.SuggestionType
import de.bixilon.minosoft.commands.util.CommandReader

class LiteralNode : ExecutableNode {
    private val suggester: ArraySuggestion<String> = ArraySuggestion(listOf(name, *aliases.toTypedArray()))

    constructor(
        name: String,
        aliases: Set<String> = setOf(),
        suggestion: SuggestionType<*>? = null,
        executable: Boolean = false,
        redirect: CommandNode? = null,
    ) : super(name, aliases, suggestion, false, null, executable, redirect)


    constructor(name: String, aliases: Set<String> = setOf(), onlyDirectExecution: Boolean = true, executor: CommandExecutor) : super(name, aliases, onlyDirectExecution = onlyDirectExecution, executor = executor, executable = true)

    constructor(builder: CommandNodeBuilder) : this(builder.name ?: throw NullPointerException("No name in builder!"), setOf(), builder.suggestionType, builder.executable)

    override fun addChild(node: CommandNode): LiteralNode {
        super.addChild(node)
        return this
    }

    private fun pushLiteralName(reader: CommandReader, stack: CommandStack): String {
        val literalName = reader.readUnquotedString() ?: throw ExpectedLiteralArgument(reader)
        if (literalName != name && literalName !in aliases) {
            throw InvalidLiteralArgumentError(reader, literalName)
        }
        stack.push(name, name)
        return name
    }


    override fun execute(reader: CommandReader, stack: CommandStack) {
        pushLiteralName(reader, stack)
        return super.execute(reader, stack)
    }

    override fun getSuggestions(reader: CommandReader, stack: CommandStack): List<Any?> {
        val literalName = reader.readUnquotedString()

        if (literalName == name || literalName in aliases) {
            stack.push(name, name)
            return super.getSuggestions(reader, stack)
        }
        return suggester.suggest(literalName) ?: listOf()
    }
}
