package de.bixilon.minosoft.config.profile.profiles.rendering

import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.rendering.advanced.AdvancedC
import de.bixilon.minosoft.config.profile.profiles.rendering.camera.CameraC
import de.bixilon.minosoft.config.profile.profiles.rendering.chunkborder.ChunkBorderC
import de.bixilon.minosoft.config.profile.profiles.rendering.experimental.ExperimentalC
import de.bixilon.minosoft.config.profile.profiles.rendering.light.LightC
import de.bixilon.minosoft.config.profile.profiles.rendering.movement.MovementC

/**
 * Profile for general rendering
 */
class RenderingProfile(
    description: String? = null,
) : Profile {
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")

    val camera = CameraC()
    val advanced = AdvancedC()
    val movement = MovementC()
    val chunkBorder = ChunkBorderC()
    val light = LightC()
    val experimental = ExperimentalC()


    override fun toString(): String {
        return RenderingProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
