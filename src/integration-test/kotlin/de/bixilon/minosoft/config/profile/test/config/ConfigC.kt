/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.profile.test.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.config.profile.Boxed
import de.bixilon.minosoft.config.profile.delegate.SimpleDelegate
import de.bixilon.minosoft.config.profile.delegate.primitive.IntDelegate
import de.bixilon.minosoft.config.profile.delegate.types.StringDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile

class ConfigC(profile: Profile) {

    @get:JsonProperty("prop")
    private var _prop by IntDelegate(profile, 0)

    @get:JsonIgnore
    var prop: Boxed? by SimpleDelegate(profile, null)

    init {
        this::prop.observe(this) { _prop = it?.value ?: 0 }
        this::_prop.observe(this) { prop = Boxed(it, false) }
    }

    var normal by StringDelegate(profile, "test")
}
