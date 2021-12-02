package de.bixilon.minosoft.config.profile

import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfile
import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfile
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfile
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfileManager

data class ProfileCollection(
    val eros: ErosProfile = ErosProfileManager.selected,
    val particle: ParticleProfile = ParticleProfileManager.selected,
    val audio: AudioProfile = AudioProfileManager.selected,
)
