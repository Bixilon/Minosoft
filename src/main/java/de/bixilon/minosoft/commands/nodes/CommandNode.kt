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

import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.util.CommandReader

abstract class CommandNode(
    val executable: Boolean,
    val redirect: CommandNode?,
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
        val pointer = reader.pointer
        val stackSize = stack.size
        var lastError: Throwable? = null
        for (child in children) {
            try {
                return executeChild(child, reader, stack)
            } catch (error: Throwable) {
                lastError = error
            }
            reader.pointer = pointer
            stack.reset(stackSize)
        }
        throw lastError ?: return
    }
}
