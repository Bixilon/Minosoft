package de.bixilon.minosoft.config.config2.config.eros.general

import de.bixilon.minosoft.config.config2.config.eros.ErosProfileManager.delegate
import java.util.*

class GeneralC2 {
    /**
     * Language to use for eros (and the fallback for the connection)
     */
    var language: Locale by delegate(Locale.getDefault())
}
