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

package de.bixilon.minosoft.config.profile.profiles.account

import com.fasterxml.jackson.annotation.JsonInclude
import de.bixilon.minosoft.config.profile.ProfileLock
import de.bixilon.minosoft.config.profile.ProfileType
import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.types.RedirectDelegate
import de.bixilon.minosoft.config.profile.delegate.types.StringDelegate
import de.bixilon.minosoft.config.profile.delegate.types.map.MapDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.storage.ProfileStorage
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

/**
 * Profile for accounts
 */
class AccountProfile(
    override var storage: ProfileStorage? = null,
) : Profile {
    override val lock = ProfileLock()

    @Deprecated("Account warning", level = DeprecationLevel.HIDDEN)
    val NOTICE by StringDelegate(this, "NEVER EVER SHARE THIS FILE WITH SOMEBODY (NOT IN ISSUES, BUG REPORTS, NOWHERE!). IF YOU DO SO, YOU PUT YOUR ACCOUNTS AT HIGH RISK!!!")


    /**
     * Before using an account, it always tries to fetch the profile.
     * If the fetch is successful, we can be sure that the account is working.
     */
    var alwaysFetchProfile by BooleanDelegate(this, true)

    /**
     * Minosoft prevents joining online servers with offline accounts.
     * If you still need to join that server (with an invalid secret key), enable it.
     */
    var ignoreNotEncryptedAccount by BooleanDelegate(this, false)

    /**
     * All accounts
     */
    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var entries: MutableMap<String, Account> by MapDelegate(this, mutableMapOf())
        private set

    var selected: Account? by RedirectDelegate<Account?, String?>(this, { it?.id }, { entries[it] })

    override fun toString(): String {
        return storage?.toString() ?: super.toString()
    }


    companion object : ProfileType<AccountProfile> {
        override val identifier = minosoft("account")
        override val clazz = AccountProfile::class.java
        override val icon: Ikon get() = FontAwesomeSolid.HEADPHONES

        override fun create(storage: ProfileStorage?) = AccountProfile(storage)
    }
}
