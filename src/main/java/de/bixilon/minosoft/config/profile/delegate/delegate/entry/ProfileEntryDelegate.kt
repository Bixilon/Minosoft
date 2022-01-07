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

package de.bixilon.minosoft.config.profile.delegate.delegate.entry

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.util.delegate.delegate.DelegateSetter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class ProfileEntryDelegate<V>(
    protected val profileManager: ProfileManager<*>,
    protected val profileName: String,
) : ReadWriteProperty<Any, V>, DelegateSetter<V> {
    protected lateinit var profile: Profile
    protected lateinit var property: KProperty<V>

    protected val profileInitialized: Boolean
        get() = this::profile.isInitialized


    protected fun checkLateinitValues(property: KProperty<*>?) {
        if (!this::profile.isInitialized) {
            profileManager.profiles[profileName]?.let { this.profile = it }
        }
        if (property != null && !this::property.isInitialized) {
            this.property = property.unsafeCast()
        }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): V {
        checkLateinitValues(property)
        return get()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        checkLateinitValues(property)
        set(value)
    }
}
