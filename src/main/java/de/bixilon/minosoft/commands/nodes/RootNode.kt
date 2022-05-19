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

import de.bixilon.minosoft.commands.nodes.builder.CommandNodeBuilder
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class RootNode : CommandNode {

    constructor() : super(false, null)
    constructor(builder: CommandNodeBuilder) : super(builder.executable, null)

    fun execute(command: String, connection: PlayConnection? = null) {
        val stack = CommandStack()
        if (connection != null) {
            stack.connection = connection
        }
        execute(CommandReader(command), stack)
    }

    fun getSuggestions(command: String): List<Any?> {
        return getSuggestions(CommandReader(command), CommandStack())
    }
}
