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

package de.bixilon.minosoft.config.profile.profiles.connection

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.connection.skin.SkinC
import de.bixilon.minosoft.data.player.Arms

/**
 * Profile for connection
 */
class ConnectionProfile(
    description: String? = null,
) : Profile {
    override val manager: ProfileManager<Profile> = ConnectionProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")

    /**
     * Language for language files.
     * Will be sent to the server
     * If unset (null), uses eros language
     */
    var language: String? by delegate(null)

    /**
     * If false, the server should not list us the ping player list
     * Will be sent to the server
     */
    var playerListing by delegate(true)

    /**
     * Main arm to use
     */
    var mainArm by delegate(Arms.RIGHT)

    val skin = SkinC()

    var autoRespawn by delegate(false)

    override fun toString(): String {
        return ConnectionProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
