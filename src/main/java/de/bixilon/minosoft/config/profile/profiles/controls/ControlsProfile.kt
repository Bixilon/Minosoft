package de.bixilon.minosoft.config.profile.profiles.controls

import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfileManager.mapDelegate
import de.bixilon.minosoft.config.profile.profiles.controls.interaction.InteractionC
import de.bixilon.minosoft.config.profile.profiles.controls.mouse.MouseC
import de.bixilon.minosoft.data.registries.ResourceLocation

/**
 * Profile for controls
 */
class ControlsProfile(
    description: String? = null,
) : Profile {
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")

    var keyBindings: MutableMap<ResourceLocation, KeyBinding> by mapDelegate()
        private set

    val mouse = MouseC()
    val interaction = InteractionC()


    override fun toString(): String {
        return ControlsProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
