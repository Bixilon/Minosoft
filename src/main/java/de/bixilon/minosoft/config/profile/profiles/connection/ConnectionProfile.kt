package de.bixilon.minosoft.config.profile.profiles.connection

import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.connection.skin.SkinC
import de.bixilon.minosoft.data.player.Arms
import java.util.*

/**
 * Profile for connection
 */
class ConnectionProfile(
    description: String? = null,
) : Profile {
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")

    /**
     * Language for language files.
     * Will be sent to the server
     * If unset (null), uses eros language
     */
    var language: Locale? by delegate(null)

    /**
     * If false, the server should not list us the ping player list
     * Will be sent to the server
     */
    var playerListing by delegate(true)

    /**
     * Main arm to use
     */
    var mainArm by delegate(Arms.RIGHT)

    val skin = SkinC()

    override fun toString(): String {
        return ConnectionProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
