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
import de.bixilon.minosoft.commands.errors.literal.TrailingTextArgument
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.util.CommandReader

abstract class CommandNode(
    val executable: Boolean,
    var redirect: CommandNode?,
) {
    protected val children: MutableList<CommandNode> = mutableListOf()

    open fun addChild(node: CommandNode): CommandNode {
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
        var errorStack = -1

        for (child in children) {
            reader.pointer = pointer
            stack.reset(stackSize)
            try {
                executeChild(child, reader, stack)
                if (reader.canPeek()) {
                    throw TrailingTextArgument(reader)
                }
                return
            } catch (error: Throwable) {
                if (stack.size > errorStack) {
                    errorStack = stack.size
                    childError = error
                }
            }
        }
        throw childError ?: return
    }


    open fun getSuggestions(reader: CommandReader, stack: CommandStack): List<Any?> {
        checkForDeadEnd(reader)
        val suggestions: MutableList<Any?> = mutableListOf()

        val pointer = reader.pointer
        val stackSize = stack.size

        var childError: Throwable? = null
        var errorStack = -1
        var parserSucceeds = 0

        for (child in children) {
            reader.pointer = pointer
            stack.reset(stackSize)
            try {
                val childSuggestions = child.getSuggestions(reader, stack)
                if (reader.canPeek()) {
                    throw TrailingTextArgument(reader)
                }
                parserSucceeds++

                if (stack.size == stackSize || stack.size == stackSize + 1) {
                    // only went 1 layer deeper, add to suggestions
                    suggestions.addAll(childSuggestions)
                    continue
                }

                return childSuggestions
            } catch (error: Throwable) {
                if (stack.size > errorStack) {
                    errorStack = stack.size
                    childError = error
                }
            }
        }
        if (parserSucceeds == 0) {
            throw childError ?: return emptyList()
        }

        return suggestions
    }

    fun clear() {
        children.clear()
    }


    protected fun checkForDeadEnd(reader: CommandReader) {
        if (children.isEmpty()) {
            if (reader.canPeek()) {
                throw TrailingTextArgument(reader)
            } else {
                throw DeadEndError(reader)
            }
        }
    }
}
