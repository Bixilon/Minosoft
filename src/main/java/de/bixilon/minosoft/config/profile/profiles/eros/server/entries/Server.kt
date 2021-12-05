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

package de.bixilon.minosoft.config.profile.profiles.eros.server.entries

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.backingDelegate
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.mapDelegate
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.data.text.ChatComponent

class Server(
    address: String,
    name: ChatComponent = ChatComponent.of(address),
    forcedVersion: Version? = null,
) {
    /**
     * Server-address as string. May contain the port
     */
    var address by delegate(address)

    /**
     * Server name (showed in eros)
     */
    var name by delegate(name)

    /**
     * Profiles to use for the connection to the server.
     * Changing profiles requires reconnect
     * If profile is unset, defaults to eros global profiles
     */
    var profiles: MutableMap<ResourceLocation, String> by mapDelegate()

    @get:JsonProperty("forced_version")
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    private var _forcedVersion by delegate(forcedVersion?.name)

    @get:JsonIgnore
    var forcedVersion by backingDelegate(getter = { Versions.getVersionByName(_forcedVersion) }, setter = { _forcedVersion = it?.name })

    @get:JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var faviconHash: String? by delegate(null)
}
