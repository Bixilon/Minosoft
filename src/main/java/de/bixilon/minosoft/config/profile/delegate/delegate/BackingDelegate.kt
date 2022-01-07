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

import de.bixilon.kutil.watcher.WatchUtil.identifier
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.delegate.ProfilesDelegateManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.util.delegate.delegate.DelegateSetter
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class BackingDelegate<V>(
    private val profileManager: ProfileManager<*>,
    private val profileName: String,
    private val verify: ((V) -> Unit)?,
) : ReadWriteProperty<Any, V>, DelegateSetter<V> {
    private lateinit var profile: Profile

    override fun getValue(thisRef: Any, property: KProperty<*>): V {
        return get()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        val previous = get()
        if (previous == value) {
            return
        }
        verify?.invoke(value)
        if (!this::profile.isInitialized) {
            val profile = profileManager.profiles[profileName] ?: return set(value)
            this.profile = profile
        }
        if (profile.initializing) {
            return set(value)
        }

        if (StaticConfiguration.LOG_DELEGATE) {
            Log.log(LogMessageType.PROFILES, LogLevels.VERBOSE) { "Changed option $property in profile $profileName from ${get()} to $value" }
        }
        if (!profile.reloading) {
            profileManager.profiles[profileName]?.saved = false
        }
        set(value)

        ProfilesDelegateManager.onChange(profile, property.identifier, previous, value)
    }
}
