package de.bixilon.minosoft.config.profile.profiles.rendering.advanced

import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager.delegate

class AdvancedC {

    /**
     * Sets the window swap interval (vsync)
     * 0 means vsync disabled
     * Every value above 0 means 1/x  * <vsync framerate>
     * Must not be negative
     */
    var swapInterval by delegate(1) { check(it >= 0) { "Swap interval must not be negative!" } }
}
