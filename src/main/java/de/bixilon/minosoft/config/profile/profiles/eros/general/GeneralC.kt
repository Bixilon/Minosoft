package de.bixilon.minosoft.config.profile.profiles.eros.general

import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.delegate
import java.util.*

class GeneralC {
    /**
     * Language to use for eros. This is also the fallback language for other profiles
     */
    var language: Locale by delegate(Locale.getDefault())
}
