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
import de.bixilon.minosoft.commands.stack.CommandExecutor
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.suggestion.types.SuggestionType
import de.bixilon.minosoft.commands.util.CommandReader

abstract class ExecutableNode(
    name: String,
    aliases: Set<String> = setOf(),
    val suggestion: SuggestionType<*>? = null,
    var onlyDirectExecution: Boolean = true,
    var executor: CommandExecutor? = null,
    executable: Boolean = executor != null,
    redirect: CommandNode? = null,
) : NamedNode(name, aliases, executable, redirect) {

    protected fun execute(stack: CommandStack) {
        try {
            executor?.invoke(stack)
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
    }

    override fun execute(reader: CommandReader, stack: CommandStack) {
        if (!reader.canPeekNext()) {
            // empty string
            if (executable) {
                return execute(stack)
            } else if (children.isEmpty()) {
                throw DeadEndError(reader)
            } else {
                throw ExpectedArgumentError(reader)
            }
        }
        super.execute(reader, stack)
    }

    override fun executeChild(child: CommandNode, reader: CommandReader, stack: CommandStack) {
        super.executeChild(child, reader, stack)
        if (!onlyDirectExecution) {
            execute(stack)
        }
    }
}
