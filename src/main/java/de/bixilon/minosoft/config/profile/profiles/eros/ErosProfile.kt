package de.bixilon.minosoft.config.profile.profiles.eros

import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.eros.general.GeneralC
import de.bixilon.minosoft.config.profile.profiles.eros.server.ServerC
import de.bixilon.minosoft.config.profile.profiles.eros.text.TextC
import de.bixilon.minosoft.config.profile.profiles.eros.theme.ThemeC
import de.bixilon.minosoft.util.KUtil.unsafeCast

/**
 * Profile for Eros
 */
class ErosProfile(
    description: String? = null,
) : Profile {
    override val manager: ProfileManager<Profile> = ErosProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")


    val general = GeneralC()
    val theme = ThemeC()
    val server = ServerC()
    val text = TextC()

    override fun toString(): String {
        return ErosProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
