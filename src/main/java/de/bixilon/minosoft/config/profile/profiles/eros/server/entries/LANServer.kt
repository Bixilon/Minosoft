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

package de.bixilon.minosoft.config.profile.profiles.eros.server.entries

import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.text.ChatComponent

class LANServer(
    address: String,
    name: ChatComponent = ChatComponent.of(address),
) : AbstractServer {

    init {
        check(address.isNotBlank())
        check(name.message.isNotBlank())
    }

    override val queryVersion: Boolean get() = true

    override val address by observed(address)

    override val name by observed(name)
    override val forcedVersion: Version? get() = null
    override val profiles: MutableMap<ResourceLocation, String> get() = mutableMapOf()

    override var faviconHash: String? by observed(null)
}
