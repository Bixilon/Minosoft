/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.collections

import de.bixilon.minosoft.util.KUtil.toSynchronizedList
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Function

class SynchronizedMap<K, V>(
    private val original: MutableMap<K, V>,
) : MutableMap<K, V> {
    private val lock = Object()
    override val size: Int
        get() = synchronized(lock) { original.size }

    override fun containsKey(key: K): Boolean {
        synchronized(lock) {
            return original.containsKey(key)
        }
    }

    override fun containsValue(value: V): Boolean {
        synchronized(lock) {
            return original.containsValue(value)
        }
    }

    override fun get(key: K): V? {
        synchronized(lock) {
            return original[key]
        }
    }

    override fun isEmpty(): Boolean {
        synchronized(lock) {
            return original.isEmpty()
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() {
            synchronized(lock) {
                return original.entries.toSynchronizedSet()
            }
        }
    override val keys: MutableSet<K>
        get() {
            synchronized(lock) {
                return original.keys.toSynchronizedSet()
            }
        }
    override val values: MutableCollection<V>
        get() {
            synchronized(lock) {
                return original.values.toSynchronizedList()
            }
        }

    override fun clear() {
        synchronized(lock) {
            original.clear()
        }
    }

    override fun put(key: K, value: V): V? {
        synchronized(lock) {
            return original.put(key, value)
        }
    }

    override fun putAll(from: Map<out K, V>) {
        synchronized(lock) {
            return original.putAll(from)
        }
    }

    override fun remove(key: K): V? {
        synchronized(lock) {
            return original.remove(key)
        }
    }

    override fun hashCode(): Int {
        synchronized(lock) {
            return original.hashCode()
        }
    }

    override fun toString(): String {
        synchronized(lock) {
            return original.toString()
        }
    }

    override fun putIfAbsent(key: K, value: V): V? {
        synchronized(lock) {
            return original.putIfAbsent(key, value)
        }
    }

    override fun forEach(action: BiConsumer<in K, in V>) {
        synchronized(lock) {
            return original.forEach(action)
        }
    }

    override fun getOrDefault(key: K, defaultValue: V): V {
        synchronized(lock) {
            return original.getOrDefault(key, defaultValue)
        }
    }

    fun getOrPut(key: K, defaultValue: () -> V): V {
        synchronized(lock) {
            var value = get(key)
            return if (value == null) {
                value = defaultValue()
                put(key, value)
                value
            } else {
                value
            }
        }
    }

    override fun remove(key: K, value: V): Boolean {
        synchronized(lock) {
            return original.remove(key, value)
        }
    }

    override fun equals(other: Any?): Boolean {
        synchronized(lock) {
            return original == other
        }
    }

    override fun replaceAll(function: BiFunction<in K, in V, out V>) {
        synchronized(lock) {
            return original.replaceAll(function)
        }
    }

    override fun compute(key: K, remappingFunction: BiFunction<in K, in V?, out V?>): V? {
        synchronized(lock) {
            return original.compute(key, remappingFunction)
        }
    }

    override fun computeIfAbsent(key: K, mappingFunction: Function<in K, out V>): V {
        synchronized(lock) {
            return original.computeIfAbsent(key, mappingFunction)
        }
    }

    override fun computeIfPresent(key: K, remappingFunction: BiFunction<in K, in V, out V?>): V? {
        synchronized(lock) {
            return original.computeIfPresent(key, remappingFunction)
        }
    }

    override fun replace(key: K, value: V): V? {
        synchronized(lock) {
            return original.replace(key, value)
        }
    }

    override fun merge(key: K, value: V, remappingFunction: BiFunction<in V, in V, out V?>): V? {
        synchronized(lock) {
            return original.merge(key, value, remappingFunction)
        }
    }

    override fun replace(key: K, oldValue: V, newValue: V): Boolean {
        synchronized(lock) {
            return original.replace(key, oldValue, newValue)
        }
    }

    fun getAndRemove(key: K): V? {
        synchronized(lock) {
            val value = this[key] ?: return null
            this.remove(key)
            return value
        }
    }
}
