/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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
import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.primitive.IntDelegate
import de.bixilon.minosoft.config.profile.delegate.types.StringDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.particle.ParticleProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.particle.types.TypesC
import de.bixilon.minosoft.gui.rendering.particle.ParticleRenderer
import java.util.concurrent.atomic.AtomicInteger

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
    override var ignoreReloads = AtomicInteger()
    override val version: Int = latestVersion
    override var description by StringDelegate(this, description ?: "")

    /**
     * Skips the loading of the particle render. Requires to reload the renderer!
     */
    var skipLoading by BooleanDelegate(this, false)

    /**
     * Enabled or disables particle renderer
     * Does not skip loading of particles
     */
    var enabled by BooleanDelegate(this, true)

    /**
     * View distance for particles
     * This is limited by the block simulation distance
     *
     * @see de.bixilon.minosoft.config.profile.profiles.block.BlockProfile.viewDistance
     */
    var viewDistance by IntDelegate(this, 10, "", arrayOf(0..128))

    /**
     * Limits the number of particles.
     * Particles that exceed that count will be ignored
     * Must not be negative or exceed $RenderConstants.MAXIMUM_PARTICLE_AMOUNT
     * @see ParticleRenderer.MAXIMUM_AMOUNT
     */
    var maxAmount by IntDelegate(this, ParticleRenderer.MAXIMUM_AMOUNT, "", arrayOf(0..ParticleRenderer.MAXIMUM_AMOUNT))
    val types = TypesC(this)

    override fun toString(): String {
        return ParticleProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
