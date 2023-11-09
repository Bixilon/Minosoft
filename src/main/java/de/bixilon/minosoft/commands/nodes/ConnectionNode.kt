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

package de.bixilon.minosoft.commands.nodes

import de.bixilon.minosoft.commands.errors.ExpectedArgumentError
import de.bixilon.minosoft.commands.nodes.builder.CommandNodeBuilder
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.util.CommandReader

open class ConnectionNode : RootNode {

    constructor() : super()
    constructor(builder: CommandNodeBuilder) : super(builder)

    override fun execute(reader: CommandReader, stack: CommandStack) {
        val rest = reader.peekRemaining() ?: throw ExpectedArgumentError(reader)

        var thrown: Throwable? = null // throw it after sending ti
        try {
            super.execute(reader, stack)
        } catch (error: Throwable) {
            thrown = error
        }
        stack.connection.util.sendCommand("$COMMAND_PREFIX$rest", stack)

        thrown?.let { throw it }
    }

    companion object {
        const val COMMAND_PREFIX = '/'
    }
}
