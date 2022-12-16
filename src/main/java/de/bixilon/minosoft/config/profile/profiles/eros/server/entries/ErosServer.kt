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

import com.fasterxml.jackson.annotation.JacksonInject
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.assets.util.FileAssetsUtil
import de.bixilon.minosoft.config.profile.delegate.BackingDelegate
import de.bixilon.minosoft.config.profile.delegate.SimpleDelegate
import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.types.NullableStringDelegate
import de.bixilon.minosoft.config.profile.delegate.types.map.MapDelegate
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfile
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions

class ErosServer(
    @JacksonInject profile: ErosProfile,
    address: String,
    name: ChatComponent = ChatComponent.of(address),
    forcedVersion: Any? = null, // must be version
    profiles: MutableMap<ResourceLocation, String> = mutableMapOf(),
    queryVersion: Boolean = true,
) : AbstractServer {

    init {
        check(forcedVersion == null || forcedVersion is Version)
    }

    /**
     * Server-address as string. May contain the port
     */
    override var address by SimpleDelegate(profile, address, "") { check(it.isNotBlank()) { "Server address must not be blank!" } }

    /**
     * Server name (showed in eros)
     */
    override var name by SimpleDelegate(profile, name, "") { check(it.message.isNotBlank()) { "Server name must not be blank!" } }

    /**
     * Sends version -1 in the handshake to query the servers version
     */
    override var queryVersion by BooleanDelegate(profile, queryVersion, "")

    /**
     * Profiles to use for the connection to the server.
     * Changing profiles requires reconnect
     * If profile is unset, defaults to eros global profiles
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    override var profiles: MutableMap<ResourceLocation, String> by MapDelegate(profile, profiles, "")

    @get:JsonProperty("forced_version")
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    private var _forcedVersion by NullableStringDelegate(profile, forcedVersion.unsafeCast<Version?>()?.name, "")

    @get:JsonIgnore
    override var forcedVersion by BackingDelegate(get = { Versions[_forcedVersion] }, set = { _forcedVersion = it?.name })

    init {
        this::_forcedVersion.observe(this) { this.forcedVersion = Versions[it] }
    }

    @get:JsonInclude(JsonInclude.Include.NON_DEFAULT)
    override var faviconHash: String? by NullableStringDelegate(profile, null, "") { if (it != null) check(it.length == FileAssetsUtil.HashTypes.SHA256.length) { "Not a valid sha256 hash!" } }
}
