package de.bixilon.minosoft.config.profile.profiles.audio

import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.audio.types.TypesC
import de.bixilon.minosoft.config.profile.profiles.audio.volume.VolumeC

/**
 * Profile for audio
 */
class AudioProfile(
    description: String? = null,
) : Profile {
    override var initializing: Boolean = true
        private set
    override var saved: Boolean = true
    override val version: Int = latestVersion
    override val description by delegate(description ?: "")

    /**
     * Skips the loading od the AudioPlayer
     * Requires reloading of the whole audio subsystem to be applied
     */
    var skipLoading by delegate(true)

    /**
     * Enabled or disables all audio playing
     * Does not skip loading of audio
     */
    var enabled by delegate(true)

    val types = TypesC()
    val volume = VolumeC()


    override fun toString(): String {
        return AudioProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
