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

import de.bixilon.minosoft.commands.nodes.builder.CommandNodeBuilder
import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.stack.CommandExecutor
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.suggestion.types.SuggestionType
import de.bixilon.minosoft.commands.util.CommandReader

class ArgumentNode : ExecutableNode {
    private val parser: ArgumentParser<*>

    constructor(
        name: String,
        parser: ArgumentParser<*>,
        suggestion: SuggestionType<*>? = null,
        executable: Boolean = false,
        redirect: CommandNode? = null,
    ) : super(name, setOf(), suggestion, false, null, executable, redirect) {
        this.parser = parser
    }


    constructor(name: String, parser: ArgumentParser<*>, onlyDirectExecution: Boolean = true, executor: CommandExecutor) : super(name, executable = true, onlyDirectExecution = onlyDirectExecution, executor = executor) {
        this.executor = executor
        this.parser = parser
    }

    constructor(builder: CommandNodeBuilder) : this(builder.name ?: throw NullPointerException("No name in builder!"), builder.parser ?: throw NullPointerException("No parser in builder!"), builder.suggestionType, builder.executable)

    override fun addChild(node: CommandNode): ArgumentNode {
        super.addChild(node)
        return this
    }

    override fun execute(reader: CommandReader, stack: CommandStack) {
        reader.skipWhitespaces(1)
        val parsed = parser.parse(reader)
        stack.push(name, parsed)
        super.execute(reader, stack)
    }

    override fun getSuggestions(reader: CommandReader, stack: CommandStack): List<Any?> {
        reader.skipWhitespaces(1)
        val pointer = reader.pointer
        val stackSize = stack.size
        try {
            // try parsing our argument
            // it should succeed if we are not the last
            val parsed = parser.parse(reader)
            stack.push(name, parsed)
            return super.getSuggestions(reader, stack)
        } catch (error: Throwable) {
            if (stack.size > stackSize + 1) {
                // we were deeper in the stack, we are not the last argument
                throw error
            }
            reader.pointer = pointer
            stack.reset(stackSize)
        }

        return parser.getSuggestions(reader)
    }
}
