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

package de.bixilon.minosoft.config.profile.profiles.eros

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.delegate.types.StringDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.eros.general.GeneralC
import de.bixilon.minosoft.config.profile.profiles.eros.server.ServerC
import de.bixilon.minosoft.config.profile.profiles.eros.text.TextC
import de.bixilon.minosoft.config.profile.profiles.eros.theme.ThemeC

/**
 * Profile for Eros
 */
class ErosProfile(
    description: String? = null,
) : Profile {
    override val manager: ProfileManager<Profile> = ErosProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreNextReload: Boolean = false
    override val version: Int = latestVersion
    override var description by StringDelegate(this, description ?: "")


    val general = GeneralC(this)
    val theme = ThemeC(this)
    val server = ServerC(this)
    val text = TextC(this)

    override fun toString(): String {
        return ErosProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
