package de.bixilon.minosoft.config.profile.profiles.eros

import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.eros.general.GeneralC

class ErosProfile(
    description: String? = null,
) : Profile {
    override var initializing: Boolean = true
        private set
    override val version: Int = latestVersion
    override val description by delegate(description ?: "")

    override var saved: Boolean = true

    val general: GeneralC = GeneralC()

    override fun toString(): String {
        return ErosProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
