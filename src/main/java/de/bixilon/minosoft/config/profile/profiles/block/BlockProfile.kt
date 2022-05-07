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

package de.bixilon.minosoft.config.profile.profiles.block

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.block.BlockProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.block.outline.OutlineC
import de.bixilon.minosoft.config.profile.profiles.block.rendering.RenderingC
import de.bixilon.minosoft.data.world.World

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
    var viewDistance by delegate(10) { check(it in 0..World.MAX_RENDER_DISTANCE) { "View distance must not be negative or exceed ${World.MAX_RENDER_DISTANCE}" } }

    /**
     * Ticking (entity, block, particle) is just applied in this distance.
     *
     * Starting from 1.18 the server can set this value per connection.
     * The lower value will be used in that case
     * For calculation see viewDistance
     * @see viewDistance
     */
    var simulationDistance by delegate(8) { check(it in 0..World.MAX_RENDER_DISTANCE) { "Simulation distance must not be negative or exceed ${World.MAX_RENDER_DISTANCE}" } }

    val outline = OutlineC()
    val rendering = RenderingC()

    override fun toString(): String {
        return BlockProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
