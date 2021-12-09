package de.bixilon.minosoft.config.profile.profiles.audio.volume

import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfileManager.delegate

class VolumeC {
    var masterVolume by delegate(1.0f) { check(it in 0.0f..1.0f) { "Audio volume must be non-negative and may not exceed 1.0" } }
}
