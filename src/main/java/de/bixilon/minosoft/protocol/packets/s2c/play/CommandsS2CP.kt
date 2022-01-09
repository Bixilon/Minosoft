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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.commands.CommandArgumentNode
import de.bixilon.minosoft.data.commands.CommandLiteralNode
import de.bixilon.minosoft.data.commands.CommandNode
import de.bixilon.minosoft.data.commands.CommandRootNode
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class CommandsS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val nodes = buffer.readCommandNodeArray()
    val root = nodes[buffer.readVarInt()].unsafeCast<CommandRootNode>()

    override fun handle(connection: PlayConnection) {
        connection.commandRoot = root
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Commands (nodes=$nodes, root=$root)" }
    }

    fun PlayInByteBuffer.readCommandNode(): CommandNode {
        val flags = readByte().toInt()
        return when (CommandNode.NodeTypes.byId(flags and 0x03)!!) {
            CommandNode.NodeTypes.ROOT -> CommandRootNode(flags, this)
            CommandNode.NodeTypes.LITERAL -> CommandLiteralNode(flags, this)
            CommandNode.NodeTypes.ARGUMENT -> CommandArgumentNode(flags, this)
        }
    }

    @JvmOverloads
    @Deprecated("refactor")
    fun PlayInByteBuffer.readCommandNodeArray(length: Int = readVarInt()): Array<CommandNode> {
        val nodes = readArray(length) { readCommandNode() }
        for (node in nodes) {
            if (node.redirectNodeId != -1) {
                node.redirectNode = nodes[node.redirectNodeId]
            }

            for (childId in node.childrenIds) {
                when (val child = nodes[childId]) {
                    is CommandArgumentNode -> {
                        node.argumentsChildren.add(child)
                    }
                    is CommandLiteralNode -> {
                        node.literalChildren[child.name] = child
                    }
                }
            }
        }

        return nodes
    }
}
