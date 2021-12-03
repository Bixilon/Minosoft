package de.bixilon.minosoft.config.profile

import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfile
import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfileManager
import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfile
import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfile
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfile
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfileManager
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager

data class ProfileCollection(
    val eros: ErosProfile = ErosProfileManager.selected,
    val particle: ParticleProfile = ParticleProfileManager.selected,
    val audio: AudioProfile = AudioProfileManager.selected,
    val entity: EntityProfile = EntityProfileManager.selected,
    val resources: ResourcesProfile = ResourcesProfileManager.selected,
    val rendering: RenderingProfile = RenderingProfileManager.selected,
)
