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

package de.bixilon.minosoft.config.profile.profiles

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonMerge
import com.fasterxml.jackson.annotation.OptBoolean
import de.bixilon.minosoft.config.profile.ProfileManager
import java.util.concurrent.atomic.AtomicInteger

interface Profile {
    @get:JsonMerge(OptBoolean.FALSE)
    val version: Int
    var description: String
    @get:JsonIgnore val manager: ProfileManager<Profile>
    @get:JsonIgnore val name: String
        get() = manager.getName(this)

    @get:JsonIgnore var saved: Boolean
    @get:JsonIgnore val initializing: Boolean
    @get:JsonIgnore var reloading: Boolean
    @get:JsonIgnore var ignoreReloads: AtomicInteger// used for saving and not instantly reloading
}
