package de.bixilon.minosoft.config.config2.config.eros

import de.bixilon.minosoft.config.config2.config.Profile
import de.bixilon.minosoft.config.config2.config.eros.ErosProfileManager.delegate
import de.bixilon.minosoft.config.config2.config.eros.ErosProfileManager.latestVersion
import de.bixilon.minosoft.config.config2.config.eros.general.GeneralC2

class ErosProfile(
    description: String? = null,
) : Profile {
    override val version: Int = latestVersion
    override val description by delegate(description ?: "")

    val general: GeneralC2 = GeneralC2()

    override fun toString(): String {
        return ErosProfileManager.getName(this)
    }
}
