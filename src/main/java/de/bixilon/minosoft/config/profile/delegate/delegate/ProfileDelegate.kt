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

package de.bixilon.minosoft.config.profile.delegate.delegate

import de.bixilon.minosoft.config.profile.ProfileManager

open class ProfileDelegate<V>(
    private var value: V,
    profileManager: ProfileManager<*>,
    profileName: String,
    verify: ((V) -> Unit)?,
) : BackingDelegate<V>(profileManager, profileName, verify) {

    override fun get() = value
    override fun set(value: V) {
        this.value = value
    }
}
