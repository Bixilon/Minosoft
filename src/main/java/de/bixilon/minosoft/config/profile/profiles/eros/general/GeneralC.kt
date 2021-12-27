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
}
