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

package de.bixilon.minosoft.gui.eros.main.play.server.type.types

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.ForcePooledRunnable
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.RemoveObserver
import de.bixilon.kutil.observer.list.ListObserver.Companion.observeList
import de.bixilon.kutil.observer.list.ListObserver.Companion.observedList
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfile
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.AbstractServer
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.ErosServer
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCard
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnectionStates
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

object CustomServerType : ServerType {
    override val icon: Ikon = FontAwesomeSolid.SERVER
    override val hidden: Boolean = false
    override var readOnly: Boolean = false
    override var servers: MutableList<ErosServer> by observedList(mutableListOf())
        private set
    override val translationKey: ResourceLocation = "minosoft:server_type.custom".toResourceLocation()
    private var profile: ErosProfile? = null

    init {
        ErosProfileManager::selected.observe(this, true) { profile ->
            servers.clear()
            servers += ErosProfileManager.selected.server.entries
            this.profile = profile

            profile.server::entries.observeList(this) {
                if (profile !== this.profile) throw RemoveObserver()
                this.servers -= it.removes
                this.servers += it.adds
            }
        }
    }

    override fun refresh(cards: List<ServerCard>) {
        for (serverCard in cards) {
            val ping = serverCard.ping
            if (ping.state != StatusConnectionStates.PING_DONE && ping.state != StatusConnectionStates.ERROR) {
                continue
            }
            DefaultThreadPool += ForcePooledRunnable {
                ping.network.disconnect()
                ping.ping()
            }
        }
    }

    override fun remove(server: AbstractServer) {
        profile?.server?.entries?.remove(server)
    }

    override fun add(name: String, address: String, forcedVersion: Version?, profiles: Map<ResourceLocation, String>, queryVersion: Boolean) {
        val profile = this.profile ?: return
        profile.server.entries += ErosServer(profile = profile, name = ChatComponent.of(name), address = address, forcedVersion = forcedVersion, profiles = profiles.toMutableMap(), queryVersion = queryVersion)
    }
}
