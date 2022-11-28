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

package de.bixilon.minosoft.gui.eros.main.play.server.type.types

import de.bixilon.kutil.observer.list.ListObserver.Companion.observedList
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileSelectEvent
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.ErosServer
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCard
import de.bixilon.minosoft.modding.EventPriorities
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnectionStates
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

object CustomServerType : ServerType {
    override val icon: Ikon = FontAwesomeSolid.SERVER
    override val hidden: Boolean = false
    override var readOnly: Boolean = false
    override var servers: MutableList<ErosServer> by observedList(ErosProfileManager.selected.server.entries)
        private set
    override val translationKey: ResourceLocation = "minosoft:server_type.custom".toResourceLocation()

    init {
        GlobalEventMaster.listen<ErosProfileSelectEvent>(priority = EventPriorities.LOW) {
            servers = ErosProfileManager.selected.server.entries
        }
    }

    override fun refresh(cards: List<ServerCard>) {
        for (serverCard in cards) {
            val ping = serverCard.ping
            if (ping.state != StatusConnectionStates.PING_DONE && ping.state != StatusConnectionStates.ERROR) {
                return
            }
            ping.network.disconnect()
            ping.ping()
        }
    }
}
