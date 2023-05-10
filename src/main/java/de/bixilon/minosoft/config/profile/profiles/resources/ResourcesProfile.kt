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

package de.bixilon.minosoft.config.profile.profiles.resources

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.types.StringDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.resources.ResourcesProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.resources.assets.AssetsC
import de.bixilon.minosoft.config.profile.profiles.resources.source.SourceC
import java.util.concurrent.atomic.AtomicInteger

/**
 * Profile for resources
 */
class ResourcesProfile(
    description: String? = null,
) : Profile {
    override val manager: ProfileManager<Profile> = ResourcesProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreReloads = AtomicInteger()
    override val version: Int = latestVersion
    override var description by StringDelegate(this, description ?: "")

    val source = SourceC(this)
    val assets = AssetsC(this)

    /**
     * If set, all downloaded assets will be checked on load.
     * Checks their size and sha1 hash.
     * Deletes and re-downloads/regenerates the asset on mismatch
     */
    var verify by BooleanDelegate(this, true)

    override fun toString(): String {
        return ResourcesProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
