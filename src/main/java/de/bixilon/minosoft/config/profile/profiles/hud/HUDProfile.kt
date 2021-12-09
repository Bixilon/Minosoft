package de.bixilon.minosoft.config.profile.profiles.hud

import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.hud.HUDProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.hud.HUDProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.hud.chat.ChatC
import de.bixilon.minosoft.config.profile.profiles.hud.crosshair.CrosshairC
import de.bixilon.minosoft.util.KUtil.unsafeCast

/**
 * Profile for hud (rendering)
 */
class HUDProfile(
    description: String? = null,
) : Profile {
    override val manager: ProfileManager<Profile> = HUDProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")

    /**
     * The scale of the hud
     * Must be non-negative
     */
    var scale by delegate(2.0f) { check(it >= 0.0f) { "HUD scale must be non-negative" } }

    val chat = ChatC()
    val crosshair = CrosshairC()

    override fun toString(): String {
        return HUDProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
