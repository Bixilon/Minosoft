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

package de.bixilon.minosoft.config.profile.profiles.eros.general

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.kutil.locale.LocaleUtil.fullName
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfile
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.mapDelegate
import de.bixilon.minosoft.data.language.LanguageManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import java.util.*

class GeneralC {
    /**
     * Language to use for eros. This is also the fallback language for other profiles
     */
    var language: String by delegate(Locale.getDefault()?.fullName ?: LanguageManager.FALLBACK_LANGUAGE)

    @get:JsonProperty("account_profile") private var _accountProfile: String? by delegate(null)

    @get:JsonIgnore var accountProfile: AccountProfile
        get() = AccountProfileManager.profiles[_accountProfile] ?: AccountProfileManager.selected
        set(value) {
            _accountProfile = AccountProfileManager.getName(value)
        }

    /**
     * Profiles to use for connections
     * If profile is not set or not found, the global default profile is used
     */
    var profileOverrides: MutableMap<ResourceLocation, String> by mapDelegate()


    /**
     * Renders the skin overlay (hat) above the head (used for avatars)
     */
    var renderSkinOverlay by delegate(true)

    /**
     * Hides eros (all eros windows) once a connection with a server is successfully established.
     * Will also show it again, once it got disconnected
     */
    var hideErosOnceConnected by delegate(false)
}
