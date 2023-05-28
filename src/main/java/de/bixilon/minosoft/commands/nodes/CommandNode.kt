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
import de.bixilon.minosoft.commands.errors.literal.TrailingTextError
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.commands.util.CommandReader

abstract class CommandNode(
    val executable: Boolean,
    var redirect: CommandNode?,
) {
    protected val children: MutableList<CommandNode> = mutableListOf()


    open fun addChild(vararg node: CommandNode): CommandNode {
        children += node
        return this
    }

    protected open fun executeChild(child: CommandNode, reader: CommandReader, stack: CommandStack) {
        child.execute(reader, stack)
    }

    open fun execute(reader: CommandReader, stack: CommandStack) {
        checkForDeadEnd(reader)
        val pointer = reader.pointer
        val stackSize = stack.size

        var childError: Throwable? = null
        var errorStack: CommandStack? = null

        for (child in (redirect?.children ?: children)) {
            reader.pointer = pointer
            stack.reset(stackSize)
            try {
                executeChild(child, reader, stack)
                if (reader.canPeek()) {
                    throw TrailingTextError(reader)
                }
                return
            } catch (error: Throwable) {
                if (errorStack == null || stack.size > errorStack.size) {
                    errorStack = stack.fork()
                    childError = error
                }
            }
        }
        errorStack?.let { stack.join(it) }
        throw childError ?: return
    }


    open fun getSuggestions(reader: CommandReader, stack: CommandStack): Collection<Suggestion> {
        checkForDeadEnd(reader)
        val suggestions: MutableList<Suggestion> = mutableListOf()

        val pointer = reader.pointer
        val stackSize = stack.size

        var childError: Throwable? = null
        var errorStack: CommandStack? = null
        var parserSucceeds = 0

        for (child in (redirect?.children ?: children)) {
            reader.pointer = pointer
            stack.reset(stackSize)
            try {
                val childSuggestions = child.getSuggestions(reader, stack)
                if (reader.canPeek()) {
                    throw TrailingTextError(reader)
                }
                parserSucceeds++

                if (stack.size == stackSize || stack.size == stackSize + 1) {
                    // only went 1 layer deeper, add to suggestions
                    suggestions.addAll(childSuggestions)
                    continue
                }

                return childSuggestions
            } catch (error: Throwable) {
                if (errorStack == null || stack.size > errorStack.size) {
                    errorStack = stack.fork()
                    childError = error
                }
            }
        }

        if (parserSucceeds == 0) {
            if (!reader.canPeek(pointer) && executable) {
                return emptyList()
            }
            errorStack?.let { stack.join(it) }

            throw childError ?: return emptyList()
        }

        return suggestions
    }

    fun clear() {
        children.clear()
    }


    protected fun checkForDeadEnd(reader: CommandReader) {
        if ((redirect?.children ?: children).isEmpty()) {
            if (reader.canPeek()) {
                throw TrailingTextError(reader)
            } else {
                throw DeadEndError(reader)
            }
        }
    }
}
