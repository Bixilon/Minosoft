package de.bixilon.minosoft.config.profile.profiles.rendering

import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.rendering.advanced.AdvancedC
import de.bixilon.minosoft.config.profile.profiles.rendering.animations.AnimationsC
import de.bixilon.minosoft.config.profile.profiles.rendering.camera.CameraC
import de.bixilon.minosoft.config.profile.profiles.rendering.chunkborder.ChunkBorderC
import de.bixilon.minosoft.config.profile.profiles.rendering.experimental.ExperimentalC
import de.bixilon.minosoft.config.profile.profiles.rendering.fog.FogC
import de.bixilon.minosoft.config.profile.profiles.rendering.light.LightC
import de.bixilon.minosoft.config.profile.profiles.rendering.movement.MovementC
import de.bixilon.minosoft.config.profile.profiles.rendering.performance.PerformanceC
import de.bixilon.minosoft.util.KUtil.unsafeCast

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
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")

    val advanced = AdvancedC()
    val animations = AnimationsC()
    val camera = CameraC()
    val chunkBorder = ChunkBorderC()
    val experimental = ExperimentalC()
    val fog = FogC()
    val light = LightC()
    val movement = MovementC()
    val performance = PerformanceC()


    override fun toString(): String {
        return RenderingProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
