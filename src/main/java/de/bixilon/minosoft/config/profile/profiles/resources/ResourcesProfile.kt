package de.bixilon.minosoft.config.profile.profiles.resources

import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.resources.source.SourceC

/**
 * Profile for resources
 */
class ResourcesProfile(
    description: String? = null,
) : Profile {
    override var initializing: Boolean = true
        private set
    override var saved: Boolean = true
    override val version: Int = latestVersion
    override val description by delegate(description ?: "")

    val source = SourceC()

    override fun toString(): String {
        return ResourcesProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
