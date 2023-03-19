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

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.commands.nodes.ArgumentNode
import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.parser.brigadier.string.StringParser
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection
import de.bixilon.minosoft.protocol.versions.Versions

object PingCommand : Command {
    override var node = LiteralNode("ping")
        .addChild(ArgumentNode("address", StringParser(StringParser.StringModes.QUOTED), onlyDirectExecution = false) { stack ->
            val address = stack.get<String>("address")!!
            val version = stack.get<String>("version")?.let { Versions[it] ?: throw IllegalArgumentException("Unknown version $it") }
            val connection = StatusConnection(address, version)
            connection::status.observe(PingCommand) { if (it == null) return@observe; stack.print.print("Received status of §e$address§r: online=§c${it.usedSlots}§r/§a${it.slots}:\n ${it.motd}") }
            connection.ping()
        }.addChild(ArgumentNode("version", StringParser(StringParser.StringModes.QUOTED), executable = true)))
}
