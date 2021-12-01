package de.bixilon.minosoft.config.profile.change

import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.change.listener.ProfileChangeListener
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.collections.SynchronizedMap
import java.lang.ref.WeakReference
import java.lang.reflect.Field

object ProfilesChangeManager {
    private val listeners: SynchronizedMap<Field, SynchronizedMap<Profile?, MutableSet<Pair<WeakReference<Any>, ProfileChangeListener<Any>>>>> = synchronizedMapOf()

    fun <T> register(reference: Any, listener: ProfileChangeListener<T>) {
        this.listeners.getOrPut(listener.field) { synchronizedMapOf() }.getOrPut(listener.profile) { synchronizedSetOf() }.add(Pair(WeakReference(reference), listener.unsafeCast()))
    }

    fun onChange(profile: Profile, field: Field, previous: Any?, value: Any?) {
        val fieldListeners = listeners[field] ?: return

        fun work(queue: MutableSet<Pair<WeakReference<Any>, ProfileChangeListener<Any>>>) {
            val toRemove: MutableSet<Pair<WeakReference<Any>, ProfileChangeListener<Any>>> = mutableSetOf()
            for (pair in queue.toSynchronizedSet()) {
                val (reference, listener) = pair
                if (reference.get() == null) {
                    toRemove += pair
                }
                listener.invoke(previous.unsafeCast(), value.unsafeCast())
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
