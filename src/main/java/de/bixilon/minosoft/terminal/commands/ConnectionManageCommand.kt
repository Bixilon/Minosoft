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

import de.bixilon.jiibles.Table
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedList
import de.bixilon.minosoft.commands.nodes.ArgumentNode
import de.bixilon.minosoft.commands.nodes.CommandNode
import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.parser.minosoft.connection.ConnectionParser
import de.bixilon.minosoft.commands.parser.minosoft.connection.ConnectionTarget
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.terminal.cli.CLI

object ConnectionManageCommand : Command {
    override var node = LiteralNode("connection")
        .addChild(
            LiteralNode("list", onlyDirectExecution = false, executor = {
                val connections = PlayConnection.ACTIVE_CONNECTIONS.toSynchronizedList()
                connections += PlayConnection.ERRORED_CONNECTIONS.toSynchronizedList()
                val filteredConnections = it.get<ConnectionTarget?>("filter")?.getConnections(connections) ?: connections
                if (filteredConnections.isEmpty()) {
                    it.print.print(TextComponent("No connection matched your filter!").color(ChatColors.RED))
                    return@LiteralNode
                }
                val table = Table(arrayOf("Id", "State", "Address"))
                for (connection in filteredConnections) {
                    table += arrayOf(connection.connectionId, connection.state, connection.address)
                }
                it.print.print(table)
            })
                .addChild(ArgumentNode("filter", ConnectionParser, executable = true)),
            LiteralNode("disconnect").apply {
                addFilter { stack, connections ->
                    var disconnects = 0
                    for (connection in connections) {
                        if (!connection.network.connected) {
                            continue
                        }
                        connection.network.disconnect()
                        disconnects++
                    }
                    stack.print.print("Disconnected from $disconnects connections.")
                }
            },
            LiteralNode("select").apply {
                addFilter { stack, connections ->
                    var toSelect: PlayConnection? = null
                    for (connection in connections) {
                        if (!connection.network.connected) {
                            continue
                        }
                        if (toSelect != null) {
                            stack.print.print(TextComponent("Can not select multiple connections!").color(ChatColors.RED))
                            return@addFilter
                        }
                        toSelect = connection
                    }
                    if (toSelect == null) {
                        stack.print.print(TextComponent("No connection matched your filter!").color(ChatColors.RED))
                        return@addFilter
                    }
                    CLI.connection = toSelect
                    stack.print.print("Selected ${toSelect.connectionId}")
                }
            },
        )


    private fun CommandNode.addFilter(executor: (stack: CommandStack, connections: Collection<PlayConnection>) -> Unit): CommandNode {
        val node = ArgumentNode("filter", ConnectionParser, executor = {
            val connections = PlayConnection.ACTIVE_CONNECTIONS.toSynchronizedList()
            connections += PlayConnection.ERRORED_CONNECTIONS.toSynchronizedList()
            val filteredConnections = it.get<ConnectionTarget?>("filter")?.getConnections(connections) ?: connections
            if (filteredConnections.isEmpty()) {
                it.print.print(TextComponent("No connection matched your filter!").color(ChatColors.RED))
                return@ArgumentNode
            }
            executor(it, connections)
        })
        addChild(node)
        return node
    }
}
