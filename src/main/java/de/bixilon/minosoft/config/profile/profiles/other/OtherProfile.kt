package de.bixilon.minosoft.config.profile.profiles.other

import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.other.log.LogC

/**
 * Profile for various things that do not fit in any other profile
 */
class OtherProfile(
    description: String? = null,
) : Profile {
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")


    /**
     * MacOS only: Ignores the warning if the jvm argument
     * -XStartOnFirstThread is not set.
     * See [#29](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/29) for more details
     */
    var ignoreXStartOnFirstThreadWarning by delegate(false)

    /**
     * Listens for servers on your LAN network
     */
    var listenLAN by delegate(true)

    val log = LogC()

    override fun toString(): String {
        return OtherProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
