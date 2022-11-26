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

package de.bixilon.minosoft.config.profile.delegate

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedSet
import de.bixilon.kutil.collections.map.SynchronizedMap
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.delegate.watcher.ProfileDelegateWatcher
import de.bixilon.minosoft.config.profile.profiles.Profile
import java.lang.ref.WeakReference

@Deprecated("")
object ProfilesDelegateManager {
    private val listeners: SynchronizedMap<String, SynchronizedMap<Profile?, MutableSet<Pair<WeakReference<Any>, ProfileDelegateWatcher<Any>>>>> = synchronizedMapOf()

    fun <T> register(reference: Any, listener: ProfileDelegateWatcher<T>) {
        this.listeners.synchronizedGetOrPut(listener.fieldIdentifier) { synchronizedMapOf() }.synchronizedGetOrPut(listener.profile) { synchronizedSetOf() }.add(Pair(WeakReference(reference), listener.unsafeCast()))
    }

    fun onChange(profile: Profile, fieldIdentifier: String, previous: Any?, value: Any?) {
        val fieldListeners = listeners[fieldIdentifier] ?: return

        fun work(queue: MutableSet<Pair<WeakReference<Any>, ProfileDelegateWatcher<Any>>>) {
            val toRemove: MutableSet<Pair<WeakReference<Any>, ProfileDelegateWatcher<Any>>> = mutableSetOf()
            for (pair in queue.toSynchronizedSet()) {
                val (reference, listener) = pair
                if (reference.get() == null) {
                    toRemove += pair
                }
                listener.invoke(previous, value)
            }
            if (toRemove.isNotEmpty()) {
                if (queue.size == toRemove.size) {
                    queue.clear()
                } else {
                    queue.removeAll(toRemove)
                }
            }
        }

        fieldListeners[profile]?.let {
            work(it)
            if (it.isEmpty()) {
                fieldListeners -= profile
            }
        }

        val manager = GlobalProfileManager.CLASS_MAPPING[profile::class.java] ?: return
        if (profile == manager.selected) {
            fieldListeners[null]?.let {
                work(it)
                if (it.isEmpty()) {
                    fieldListeners -= null
                }
            }
        }
    }
}
