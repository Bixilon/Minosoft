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

package de.bixilon.minosoft.commands.stack

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.commands.nodes.NamedNode
import de.bixilon.minosoft.commands.nodes.SignedNode
import de.bixilon.minosoft.commands.stack.print.PrintTarget
import de.bixilon.minosoft.commands.stack.print.SystemPrintTarget
import de.bixilon.minosoft.data.chat.signature.signer.MessageSigner
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.security.PrivateKey
import java.time.Instant

class CommandStack(
    connection: PlayConnection? = null,
    val print: PrintTarget = SystemPrintTarget,
) {
    private val stack: MutableList<CommandStackEntry> = mutableListOf()
    val size: Int get() = stack.size

    var executor: Entity? = null
    lateinit var connection: PlayConnection

    init {
        if (connection != null) {
            this.connection = connection
        }
    }

    inline operator fun <reified T> get(name: String): T? {
        return getAny(name).nullCast()
    }

    fun getAny(name: String): Any? {
        return stack.find { it.node.name == name }?.data
    }

    fun reset(size: Int) {
        var index = 0
        stack.removeAll { index++ >= size }
    }

    fun push(node: NamedNode, data: Any?) {
        stack.add(CommandStackEntry(node, data))
    }

    fun sign(signer: MessageSigner, key: PrivateKey, salt: Long, time: Instant): Map<String, ByteArray> {
        val output: MutableMap<String, ByteArray> = mutableMapOf()
        for (entry in stack) {
            if (entry.node !is SignedNode || !entry.node.sign) {
                continue
            }
            // TODO: properly sign
            output[entry.node.name] = entry.sign(connection, signer, key, salt, time)
        }
        return output
    }
}
