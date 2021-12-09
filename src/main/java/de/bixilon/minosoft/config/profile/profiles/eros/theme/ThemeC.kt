package de.bixilon.minosoft.config.profile.profiles.eros.theme

import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.delegate

class ThemeC {

    /**
     * Name of the theme css file
     * Located in minosoft:eros/themes/<name>.css
     */
    var theme by delegate("default")
}
