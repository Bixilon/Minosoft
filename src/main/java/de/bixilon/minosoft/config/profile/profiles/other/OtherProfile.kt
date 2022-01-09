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

package de.bixilon.minosoft.config.profile.profiles.other

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.other.log.LogC

/**
 * Profile for various things that do not fit in any other profile
 */
class OtherProfile(
    description: String? = null,
) : Profile {
    override val manager: ProfileManager<Profile> = OtherProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")

    /**
     * Use native network transport if available
     */
    var epoll by delegate(true)


    /**
     * MacOS only: Ignores the warning if the jvm argument
     * -XStartOnFirstThread is not set.
     * See [#29](https://gitlab.bixilon.de/bixilon/minosoft/-/issues/29) for more details
     */
    var ignoreXStartOnFirstThreadWarning by delegate(false)

    /**
     * Listens for servers on your LAN network
     */
    var listenLAN by delegate(true)

    val log = LogC()

    override fun toString(): String {
        return OtherProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
