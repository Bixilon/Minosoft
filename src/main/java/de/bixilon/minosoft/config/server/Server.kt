/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.server

import com.squareup.moshi.Json
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.assets.AssetsUtil
import de.bixilon.minosoft.data.assets.FileAssetsManager
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCard
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnectionStates
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import java.util.*

data class Server(
    val id: Int = ++nextServerId, // ToDo: Is duplicated in config (first key, then in value)
    var address: String,
    var name: ChatComponent = ChatComponent.of(address),
    @Json(name = "version") var forcedVersion: Version? = null,
    @Json(name = "favicon") var faviconHash: String? = null,
) {
    @Transient
    var favicon: ByteArray? = null
        set(value) {
            if (Arrays.equals(field, value)) {
                return
            }
            field = value
            faviconHash?.let {
                // ToDo: Check if other server is using favicon
                AssetsUtil.deleteAsset(it, true)
            }
            if (value == null) {
                faviconHash = null
                return
            }

            this.faviconHash = FileAssetsManager.saveAsset(value, true)
        }

    @Transient
    val connections: MutableSet<PlayConnection> = synchronizedSetOf()

    @Transient
    var ping: StatusConnection? = null

    @Transient
    var card: ServerCard? = null

    fun canConnect(selectedAccount: Account): Boolean {
        return (ping?.state === StatusConnectionStates.PING_DONE
                && ((forcedVersion ?: ping?.serverVersion) != null))
                && this !in selectedAccount.connections
    }

    init {
        if (id > nextServerId) {
            nextServerId = id + 1
        }
        try {
            faviconHash?.let { favicon = AssetsUtil.readAsset(it, true) }
        } catch (exception: Exception) {
            exception.printStackTrace()
            faviconHash = null
        }
    }


    @Synchronized
    fun ping(): StatusConnection {
        var ping = ping
        if (ping == null) {
            ping = StatusConnection(address)
            this.ping = ping
            card?.let { it.serverListStatusInvoker?.let { invoker -> ping.registerEvent(invoker) } }
            ping.ping()
        }


        return ping
    }

    companion object {
        private var nextServerId = 0
    }
}
