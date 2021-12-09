package de.bixilon.minosoft.config.profile.profiles.block

import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.block.outline.OutlineC
import de.bixilon.minosoft.config.profile.profiles.block.rendering.RenderingC
import de.bixilon.minosoft.util.KUtil.unsafeCast

/**
 * Profile for block rendering
 */
class BlockProfile(
    description: String? = null,
) : Profile {
    override val manager: ProfileManager<Profile> = BlockProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")

    /**
     * The block view distance in chunks.
     * The own chunk get loaded at 0 view distance. Every value above 1 shows 1 extra ring of chunks
     * Total chunks is calculated as (viewDistance * 2 + 1)^2
     * Must not be negative or exceed 128
     *
     * Other profiles (like entities, ...) also have view distance, but this value is the only one that gets sent to the server.
     * The server may limit the other view distances according to this value
     */
    var viewDistance by delegate(10) { check(it in 0..128) { "Invalid view distance $it" } }

    /**
     * Ticking (entity, block, particle) is just applied in this distance.
     *
     * Starting from 1.18 the server can set this value per connection.
     * The lower value will be used in that case
     * For calculation see viewDistance
     * @see viewDistance
     */
    var simulationDistance by delegate(8) { check(it in 0..viewDistance) { "Simulation distance must not be negative or exceed the view distance" } }

    val outline = OutlineC()
    val rendering = RenderingC()

    override fun toString(): String {
        return BlockProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
