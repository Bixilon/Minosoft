package de.bixilon.minosoft.config.profile.profiles.controls.interaction

import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfileManager.delegate

class InteractionC {

    /**
     * Enables or disables right-clicking with a shovel on grass (…) to create grass paths
     */
    var flattening by delegate(true)

    /**
     * Enables right-clicking with an axe on any logs to create stripped logs
     */
    var stripping by delegate(true)

    /**
     * Enables right-clicking with a hoe on grass (…) to create farmland
     */
    var tilling by delegate(true)
}
