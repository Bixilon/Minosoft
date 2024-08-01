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

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.commands.nodes.ArgumentNode
import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.parser.brigadier.string.StringParser
import de.bixilon.minosoft.commands.stack.print.PrintTarget
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.protocol.address.ServerAddress
import de.bixilon.minosoft.protocol.connection.NetworkConnection
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.status.StatusSession
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.util.DNSUtil

object ConnectCommand : Command {
    override var node = LiteralNode("connect")
        .addChild(ArgumentNode("address", StringParser(StringParser.StringModes.QUOTED), allowArguments = true) { stack ->
            val address = stack.get<String>("address")!!
            val version = stack.get<String>("version")?.let { Versions[it] ?: throw CommandException("Unknown version $it") }
            val account = AccountProfileManager.selected.selected ?: throw CommandException("No account selected!")
            DefaultThreadPool += add@{
                if (version == null) {
                    stack.print.print("Pinging server to get version...")
                    val ping = StatusSession(address)
                    ping::status.observe(this) { connect(stack.print, ping.connection!!.address, ping.serverVersion ?: throw IllegalArgumentException("Could not determinate server's version!"), account) }
                    ping::error.observe(this) { stack.print.print("Could not ping $address: $it") }
                    ping.ping()
                    return@add
                }

                connect(stack.print, DNSUtil.resolveServerAddress(address).first(), version, account)
            }
        }.addChild(ArgumentNode("version", StringParser(StringParser.StringModes.QUOTED), executable = true)))


    private fun connect(print: PrintTarget, address: ServerAddress, version: Version, account: Account) {
        print.print("Connecting to $address")

        val session = PlaySession(NetworkConnection(address, true), account, version) // TODO: native network
        session.connect()
    }
}
