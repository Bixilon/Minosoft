/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.profile.profiles.particle

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.ProfileManager
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
    override val manager: ProfileManager<Profile> = ParticleProfileManager.unsafeCast()
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
