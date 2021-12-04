package de.bixilon.minosoft.config.profile.profiles.eros

import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.eros.general.GeneralC
import de.bixilon.minosoft.config.profile.profiles.eros.server.ServerC
import de.bixilon.minosoft.config.profile.profiles.eros.text.TextC

/**
 * Profile for Eros
 */
class ErosProfile(
    description: String? = null,
) : Profile {
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")


    val general = GeneralC()
    val server = ServerC()
    val text = TextC()

    override fun toString(): String {
        return ErosProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
