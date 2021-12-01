package de.bixilon.minosoft.config.profile.profiles.eros.general

import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.delegate
import java.util.*

class GeneralC {
    /**
     * Language to use for eros (and the fallback for the connection)
     */
    var language: Locale by delegate(Locale.getDefault())
}
