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

package de.bixilon.minosoft.gui.eros.main.play.server.card

import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.collections.map.SynchronizedMap
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.AbstractServer
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.status.StatusSession

class ServerCard(
    val server: AbstractServer,
) {
    val ping: StatusSession = StatusSession(server.address, if (server.queryVersion) null else server.forcedVersion)
    val sessions: MutableSet<PlaySession> = synchronizedSetOf()
    private var pinged = false


    init {
        CARDS[server] = this
    }

    @Synchronized
    fun ping() {
        if (pinged) {
            return
        }
        DefaultThreadPool += { ping.ping() }
        pinged = true
    }


    fun canConnect(account: Account?): ConnectError? {
        if (account == null) return ConnectError.NO_ACCOUNT
        if (server in account.sessions) return ConnectError.ALREADY_CONNECTED
        if (server.forcedVersion == null && ping.serverVersion == null) {
            return ConnectError.UNKNOWN_VERSION
        }
        return null
    }


    companion object {
        val CARDS: SynchronizedMap<AbstractServer, ServerCard> = synchronizedMapOf()
    }
}
