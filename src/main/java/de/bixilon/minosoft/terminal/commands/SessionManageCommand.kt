/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.terminal.commands

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedList
import de.bixilon.minosoft.commands.nodes.ArgumentNode
import de.bixilon.minosoft.commands.nodes.CommandNode
import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.parser.minosoft.session.SessionParser
import de.bixilon.minosoft.commands.parser.selector.AbstractTarget
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.terminal.cli.CLI
import de.bixilon.minosoft.util.KUtil.table

object SessionManageCommand : Command {
    override var node = LiteralNode("session", aliases = setOf("connection"))
        .addChild(
            LiteralNode("list", allowArguments = true, executor = {
                val filtered = it.collect()
                if (filtered.isEmpty()) throw CommandException("No session matched your filter!")

                it.print.print(table(filtered, "Id", "State", "Address") { c -> arrayOf(c.id, c.state, c.connection.identifier) })
            })
                .addChild(ArgumentNode("filter", SessionParser, executable = true)),
            LiteralNode("terminate", aliases = setOf("disconnect")).apply {
                addFilter { stack, sessions ->
                    var count = 0
                    sessions.forEach { it.terminate(); count++ }
                    stack.print.print("Terminated $count sessions.")
                }
            },
            LiteralNode("select").apply {
                addFilter(false) { stack, sessions ->
                    val session = sessions.first()
                    if (!session.connection.active) {
                        throw CommandException("Session $session not established anymore!")
                    }
                    CLI.session = session
                    stack.print.print("Selected ${session.id}")
                }
            },
        )


    private fun CommandNode.addFilter(multi: Boolean = true, executor: (stack: CommandStack, sessions: Collection<PlaySession>) -> Unit): CommandNode {
        val node = ArgumentNode("filter", SessionParser, executor = {
            val filtered = it.collect()
            if (filtered.isEmpty()) throw CommandException("No session matched your filter!")
            if (!multi && filtered.size > 1) throw CommandException("Can not select multiple connections!")
            executor(it, filtered)
        })
        addChild(node)
        return node
    }


    private fun CommandStack.collect(): List<PlaySession> {
        val sessions = PlaySession.ACTIVE_CONNECTIONS.toSynchronizedList()
        sessions += PlaySession.ERRORED_CONNECTIONS.toSynchronizedList()
        if (sessions.isEmpty()) throw CommandException("Not sessions available!")
        return this.get<AbstractTarget<PlaySession>?>("filter")?.filter(sessions) ?: sessions
    }
}
