package de.bixilon.minosoft.config.profile.profiles.eros

import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.eros.general.GeneralC
import de.bixilon.minosoft.config.profile.profiles.eros.server.ServerC

class ErosProfile(
    description: String? = null,
) : Profile {
    override var initializing: Boolean = true
        private set
    override var saved: Boolean = true
    override val version: Int = latestVersion
    override val description by delegate(description ?: "")


    val general: GeneralC = GeneralC()
    val server: ServerC = ServerC()

    override fun toString(): String {
        return ErosProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
