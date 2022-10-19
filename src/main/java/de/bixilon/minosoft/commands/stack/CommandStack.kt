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

package de.bixilon.minosoft.commands.stack

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.commands.stack.print.PrintTarget
import de.bixilon.minosoft.commands.stack.print.SystemPrintTarget
import de.bixilon.minosoft.data.chat.signature.MessageChain
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
        return stack.find { it.name == name }?.data
    }

    fun reset(size: Int) {
        var index = 0
        stack.removeAll { index++ >= size }
    }

    fun push(name: String, data: Any?) {
        stack.add(CommandStackEntry(name, data))
    }

    fun sign(chain: MessageChain, key: PrivateKey, salt: Long, time: Instant): Map<String, ByteArray> {
        val output: MutableMap<String, ByteArray> = mutableMapOf()
        for (entry in stack) {
            output[entry.name] = entry.sign(connection, chain, key, salt, time)
        }
        return output
    }
}
