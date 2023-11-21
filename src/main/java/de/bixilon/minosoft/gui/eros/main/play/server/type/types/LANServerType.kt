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

import de.bixilon.kutil.collections.CollectionUtil.synchronizedListOf
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.observer.list.ListObserver.Companion.observedList
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.AbstractServer
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCard
import de.bixilon.minosoft.protocol.protocol.LANServerListener
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

object LANServerType : ServerType {
    override val icon: Ikon = FontAwesomeSolid.NETWORK_WIRED
    override val hidden: Boolean
        get() = !LANServerListener.listening
    override var readOnly: Boolean = true
    override val servers: MutableList<AbstractServer> by observedList(synchronizedListOf())
    override val translationKey: ResourceLocation = "minosoft:server_type.lan".toResourceLocation()

    override fun refresh(cards: List<ServerCard>) {
        LANServerListener.clear()
    }

    override fun remove(server: AbstractServer) = Broken("read only?")
    override fun add(name: String, address: String, forcedVersion: Version?, profiles: Map<ResourceLocation, String>, queryVersion: Boolean) = Broken("read only")
}
