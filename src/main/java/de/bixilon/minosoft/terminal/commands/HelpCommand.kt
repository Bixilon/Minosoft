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

package de.bixilon.minosoft.terminal.commands

import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.stack.print.PrintTarget

object HelpCommand : Command {
    override var node: LiteralNode = LiteralNode("help", setOf("?"), executor = { it.print.overview() })
        .addChild(LiteralNode("account", executor = { it.print.account() }))

    private fun PrintTarget.overview() {
        print("-------------- Minosoft help --------------")
        print("Tip: You can always press [tab] to auto complete commands and see what (sub)commands are available!")
        print("Another tip: The help command has subcommands (e.g. type help account)")
        print("Filters: Most commands takes filters. That can either be an identifier of something or a selector (like @). Selectors can even have properties like @[type=offline]")
        print("Here are some useful commands:")
        print("  account [add|remove|list|select]      - Manages your accounts")
        print("  connection [list|disconnect|select]   - Manages the connection to a server")
        print("  connect [address] <version>           - Connects to a server")
        print("  ping [address] <version>              - Shows the motd of a server")
        print("  *say* [message]                       - Lets you write something in the chat. Requires a selected connection.")
        print("  about                                 - Shows some useful information")
    }

    private fun PrintTarget.account() {
        print("-------------- Account help --------------")
        print("account add offline [username]          - Adds a new offline account")
        print("account add microsoft                   - Adds a new microsoft account")
        print("account list <filter>                   - Lists all accounts")
        print("account select <filter>                 - Selects an account")
        print("account remove <filter>                 - Removes an account")
    }
}
