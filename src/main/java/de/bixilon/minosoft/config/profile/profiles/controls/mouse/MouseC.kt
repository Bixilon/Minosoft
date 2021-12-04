package de.bixilon.minosoft.config.profile.profiles.controls.mouse

import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfileManager.delegate

class MouseC {

    /**
     * Mouse sensitivity in percent
     * Controls how fast the mouse rotates the player around
     * Must be non-negative
     */
    var sensitivity by delegate(1.0f) { check(it > 0.0f) { "Must be non-negative!" } }

    /**
     * Controls how fast you scroll (e.g. in the hotbar)
     * Must be non-negative
     */
    var scrollSensitivity by delegate(1.0) { check(it > 0.0) { "Must be non-negative!" } }
}
