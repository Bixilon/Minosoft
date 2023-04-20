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

package de.bixilon.minosoft.config.profile.profiles.rendering

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.delegate.types.StringDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.rendering.advanced.AdvancedC
import de.bixilon.minosoft.config.profile.profiles.rendering.animations.AnimationsC
import de.bixilon.minosoft.config.profile.profiles.rendering.camera.CameraC
import de.bixilon.minosoft.config.profile.profiles.rendering.chunkborder.ChunkBorderC
import de.bixilon.minosoft.config.profile.profiles.rendering.experimental.ExperimentalC
import de.bixilon.minosoft.config.profile.profiles.rendering.fog.FogC
import de.bixilon.minosoft.config.profile.profiles.rendering.light.LightC
import de.bixilon.minosoft.config.profile.profiles.rendering.movement.MovementC
import de.bixilon.minosoft.config.profile.profiles.rendering.overlay.OverlayC
import de.bixilon.minosoft.config.profile.profiles.rendering.performance.PerformanceC
import de.bixilon.minosoft.config.profile.profiles.rendering.sky.SkyC
import java.util.concurrent.atomic.AtomicInteger

/**
 * Profile for general rendering
 */
class RenderingProfile(
    description: String? = null,
) : Profile {
    override val manager: ProfileManager<Profile> = RenderingProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreReloads = AtomicInteger()
    override val version: Int = latestVersion
    override var description by StringDelegate(this, description ?: "")

    val advanced = AdvancedC(this)
    val animations = AnimationsC(this)
    val camera = CameraC(this)
    val chunkBorder = ChunkBorderC(this)
    val experimental = ExperimentalC(this)
    val fog = FogC(this)
    val light = LightC(this)
    val movement = MovementC(this)
    val performance = PerformanceC(this)
    val overlay = OverlayC(this)
    val sky = SkyC(this)


    override fun toString(): String {
        return RenderingProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
