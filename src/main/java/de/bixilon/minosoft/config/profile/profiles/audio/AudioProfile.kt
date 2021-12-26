package de.bixilon.minosoft.config.profile.profiles.audio

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.ProfileManager
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
    override val manager: ProfileManager<Profile> = AudioProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")

    /**
     * Skips the loading od the AudioPlayer
     * Requires reloading of the whole audio subsystem to be applied
     */
    var skipLoading by delegate(false)

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
