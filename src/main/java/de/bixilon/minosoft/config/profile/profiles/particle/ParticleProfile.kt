package de.bixilon.minosoft.config.profile.profiles.particle

import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.particle.types.TypesC
import de.bixilon.minosoft.gui.rendering.RenderConstants

/**
 * Profile for particle
 */
class ParticleProfile(
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
     * Skips the loading of the particle render. Requires to reload the renderer!
     */
    var skipLoading by delegate(false)

    /**
     * Enabled or disables particle renderer
     * Does not skip loading of particles
     */
    var enabled by delegate(true)

    /**
     * View distance for particles
     * This is limited by the block simulation distance
     *
     * @see de.bixilon.minosoft.config.profile.profiles.block.BlockProfile.viewDistance
     */
    var viewDistance by delegate(10) { check(it in 0..128) { "Distance must be non-negative and must not exceed 128" } }

    /**
     * Limits the number of particles.
     * Particles that exceed that count will be ignored
     * Must not be negative or exceed $RenderConstants.MAXIMUM_PARTICLE_AMOUNT
     * @see RenderConstants.MAXIMUM_PARTICLE_AMOUNT
     */
    var maxAmount by delegate(RenderConstants.MAXIMUM_PARTICLE_AMOUNT) { check(it in 0..RenderConstants.MAXIMUM_PARTICLE_AMOUNT) { "Particle amount must be non-negative and may not exceed ${RenderConstants.MAXIMUM_PARTICLE_AMOUNT}" } }

    val types = TypesC()

    override fun toString(): String {
        return ParticleProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
