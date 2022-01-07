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

package de.bixilon.minosoft.config.profile.profiles.hud

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.hud.HUDProfileManager.delegate
import de.bixilon.minosoft.config.profile.profiles.hud.HUDProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.hud.chat.ChatC
import de.bixilon.minosoft.config.profile.profiles.hud.crosshair.CrosshairC

/**
 * Profile for hud (rendering)
 */
class HUDProfile(
    description: String? = null,
) : Profile {
    override val manager: ProfileManager<Profile> = HUDProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by delegate(description ?: "")

    /**
     * The scale of the hud
     * Must be non-negative
     */
    var scale by delegate(2.0f) { check(it >= 0.0f) { "HUD scale must be non-negative" } }

    val chat = ChatC()
    val crosshair = CrosshairC()

    override fun toString(): String {
        return HUDProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
