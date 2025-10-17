/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.terminal.arguments.connect

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.shutdown.AbstractShutdownReason
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.protocol.network.NetworkConnection
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.status.StatusSession
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.util.DNSUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ConnectArgument : OptionGroup(), AutoConnectFactory {
    val address by option("--address").required()

    private fun create(account: Account): PlaySession {
        val latch = SimpleLatch(1)

        Log.log(LogMessageType.AUTO_CONNECT, LogLevels.INFO) { "Pinging server $address to get version..." }
        val ping = StatusSession(address)
        ping::status.observe(this) { latch.dec() }
        ping::error.observe(this) { it?.printStackTrace(); ShutdownManager.shutdown(it?.message, AbstractShutdownReason.CRASH) }
        ping.ping()

        latch.await()


        return PlaySession(
            connection = NetworkConnection(ping.address!!, true), // TODO: native network (from config)
            account = account,
            version = ping.serverVersion!!,
        )
    }

    override fun create(version: Version, account: Account): PlaySession {
        if (version == Versions.AUTOMATIC) {
            return create(account)
        }
        Log.log(LogMessageType.AUTO_CONNECT, LogLevels.INFO) { "Connecting to $address, with version $version using account $account..." }
        return PlaySession(
            connection = NetworkConnection(DNSUtil.resolve(address).first(), true), // TODO: native network (from config)
            account = account,
            version = version,
        )
    }
}
