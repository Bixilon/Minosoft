package de.bixilon.minosoft.config.profile.profiles.account

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager.backingDelegate
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager.mapDelegate
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.Util

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

    var clientToken by delegate(Util.generateRandomString(128))
    var entries: MutableMap<String, Account> by mapDelegate()
        private set

    @get:JsonProperty("selected") private var _selected: String? by delegate(null)

    @get:JsonIgnore var selected: Account? by backingDelegate(getter = { entries[_selected] }, setter = { _selected = it?.id })


    override fun toString(): String {
        return AccountProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
