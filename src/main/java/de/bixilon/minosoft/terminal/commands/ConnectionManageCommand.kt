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

package de.bixilon.minosoft.terminal.commands

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedList
import de.bixilon.minosoft.commands.nodes.ArgumentNode
import de.bixilon.minosoft.commands.nodes.CommandNode
import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.parser.minosoft.connection.ConnectionParser
import de.bixilon.minosoft.commands.parser.selector.AbstractTarget
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.terminal.cli.CLI
import de.bixilon.minosoft.util.KUtil.table

object ConnectionManageCommand : Command {
    override var node = LiteralNode("connection")
        .addChild(
            LiteralNode("list", allowArguments = true, executor = {
                val filtered = it.collect()
                if (filtered.isEmpty()) throw CommandException("No connection matched your filter!")

                it.print.print(table(filtered, "Id", "State", "Address") { c -> arrayOf(c.connectionId, c.state, c.address) })
            })
                .addChild(ArgumentNode("filter", ConnectionParser, executable = true)),
            LiteralNode("disconnect").apply {
                addFilter { stack, connections ->
                    var count = 0
                    connections.filter { it.network.connected }.forEach { it.disconnect(); count++ }
                    stack.print.print("Disconnected from $count connections.")
                }
            },
            LiteralNode("select").apply {
                addFilter(false) { stack, connections ->
                    val connection = connections.first()
                    if (!connection.network.connected) {
                        throw CommandException("Not connected to $connection (anymore)!")
                    }
                    CLI.connection = connection
                    stack.print.print("Selected ${connection.connectionId}")
                }
            },
        )


    private fun CommandNode.addFilter(multi: Boolean = true, executor: (stack: CommandStack, connections: Collection<PlayConnection>) -> Unit): CommandNode {
        val node = ArgumentNode("filter", ConnectionParser, executor = {
            val filtered = it.collect()
            if (filtered.isEmpty()) throw CommandException("No connection matched your filter!")
            if (!multi && filtered.size > 1) throw CommandException("Can not select multiple connections!")
            executor(it, filtered)
        })
        addChild(node)
        return node
    }


    private fun CommandStack.collect(): List<PlayConnection> {
        val connections = PlayConnection.ACTIVE_CONNECTIONS.toSynchronizedList()
        connections += PlayConnection.ERRORED_CONNECTIONS.toSynchronizedList()
        if (connections.isEmpty()) throw CommandException("Not connections available!")
        return this.get<AbstractTarget<PlayConnection>?>("filter")?.filter(connections) ?: connections
    }
}
