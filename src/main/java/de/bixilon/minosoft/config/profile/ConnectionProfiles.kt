package de.bixilon.minosoft.config.profile

import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfile
import de.bixilon.minosoft.config.profile.profiles.audio.AudioProfileManager
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfile
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfileManager
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfile
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfileManager
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfile
import de.bixilon.minosoft.config.profile.profiles.controls.ControlsProfileManager
import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfile
import de.bixilon.minosoft.config.profile.profiles.entity.EntityProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfile
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.hud.HUDProfile
import de.bixilon.minosoft.config.profile.profiles.hud.HUDProfileManager
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfile
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfile
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfileManager
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfile
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager
import de.bixilon.minosoft.data.registries.ResourceLocation

class ConnectionProfiles(
    overrides: Map<ResourceLocation, String> = mapOf(),
    val eros: ErosProfile = overrides[ErosProfileManager.namespace]?.let { return@let ErosProfileManager.profiles[it] } ?: ErosProfileManager.selected,
    val particle: ParticleProfile = overrides[ParticleProfileManager.namespace]?.let { return@let ParticleProfileManager.profiles[it] } ?: ParticleProfileManager.selected,
    val audio: AudioProfile = overrides[AudioProfileManager.namespace]?.let { return@let AudioProfileManager.profiles[it] } ?: AudioProfileManager.selected,
    val entity: EntityProfile = overrides[EntityProfileManager.namespace]?.let { return@let EntityProfileManager.profiles[it] } ?: EntityProfileManager.selected,
    val resources: ResourcesProfile = overrides[ResourcesProfileManager.namespace]?.let { return@let ResourcesProfileManager.profiles[it] } ?: ResourcesProfileManager.selected,
    val rendering: RenderingProfile = overrides[RenderingProfileManager.namespace]?.let { return@let RenderingProfileManager.profiles[it] } ?: RenderingProfileManager.selected,
    val block: BlockProfile = overrides[BlockProfileManager.namespace]?.let { return@let BlockProfileManager.profiles[it] } ?: BlockProfileManager.selected,
    val connection: ConnectionProfile = overrides[ConnectionProfileManager.namespace]?.let { return@let ConnectionProfileManager.profiles[it] } ?: ConnectionProfileManager.selected,
    val hud: HUDProfile = overrides[HUDProfileManager.namespace]?.let { return@let HUDProfileManager.profiles[it] } ?: HUDProfileManager.selected,
    val controls: ControlsProfile = overrides[ControlsProfileManager.namespace]?.let { return@let ControlsProfileManager.profiles[it] } ?: ControlsProfileManager.selected,
    val other: OtherProfile = overrides[OtherProfileManager.namespace]?.let { return@let OtherProfileManager.profiles[it] } ?: OtherProfileManager.selected,
)
