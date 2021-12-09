package de.bixilon.minosoft.config.profile.profiles.resources

import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.resources.source.SourceC
import de.bixilon.minosoft.util.KUtil.unsafeCast

/**
 * Profile for resources
 */
class ResourcesProfile(
    description: String? = null,
) : Profile {
    override val manager: ProfileManager<Profile> = ResourcesProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")

    val source = SourceC()

    /**
     * If set, all downloaded assets will be checked on load.
     * Checks their size and sha1 hash.
     * Deletes and re-downloads/regenerates the asset on mismatch
     */
    var verify by delegate(true)

    override fun toString(): String {
        return ResourcesProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
