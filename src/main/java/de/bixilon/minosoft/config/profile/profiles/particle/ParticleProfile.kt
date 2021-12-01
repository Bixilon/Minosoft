package de.bixilon.minosoft.config.profile.profiles.particle

import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfileManager.latestVersion
import de.bixilon.minosoft.gui.rendering.RenderConstants

class ParticleProfile(
    description: String? = null,
) : Profile {
    override var initializing: Boolean = true
        private set
    override var saved: Boolean = true
    override val version: Int = latestVersion
    override val description by delegate(description ?: "")


    var enabled by delegate(true)
    var maxAmount by delegate(RenderConstants.MAXIMUM_PARTICLE_AMOUNT) { check(it in 0..RenderConstants.MAXIMUM_PARTICLE_AMOUNT) { "Particle amount must be non-negative and may not exceed ${RenderConstants.MAXIMUM_PARTICLE_AMOUNT}" } }


    override fun toString(): String {
        return ParticleProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
