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

package de.bixilon.minosoft.config.profile.profiles.account

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.random.RandomStringUtil.randomString
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager.backingDelegate
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager.mapDelegate
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.util.KUtil

/**
 * Profile for accounts
 */
class AccountProfile(
    description: String? = null,
) : Profile {
    override val manager: ProfileManager<Profile> = AccountProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")

    /**
     * The client token.
     * This 128 length long string is generated randomly while the profile was created
     * Will be sent to mojang when logging in/refreshing an account
     */
    var clientToken by delegate(KUtil.RANDOM.randomString(128))

    /**
     * All accounts
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var entries: MutableMap<String, Account> by mapDelegate()
        private set

    /**
     * The current id of the selected account
     */
    @get:JsonInclude(JsonInclude.Include.NON_NULL)
    @get:JsonProperty("selected") private var _selected: String? by delegate(null)

    @get:JsonIgnore var selected: Account? by backingDelegate(getter = { entries[_selected] }, setter = { _selected = it?.id })


    override fun toString(): String {
        return AccountProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
