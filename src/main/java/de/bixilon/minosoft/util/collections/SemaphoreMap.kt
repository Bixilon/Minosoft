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
import de.bixilon.minosoft.util.SemaphoreLock
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Function

class SemaphoreMap<K, V>(
    private val original: MutableMap<K, V>,
) : MutableMap<K, V> {
    val lock = SemaphoreLock()
    override val size: Int
        get() {
            lock.acquire()
            val returnValue = original.size
            lock.release()
            return returnValue
        }

    override fun containsKey(key: K): Boolean {
        lock.acquire()
        val returnValue = original.containsKey(key)
        lock.release()
        return returnValue
    }

    override fun containsValue(value: V): Boolean {
        lock.acquire()
        val returnValue = original.containsValue(value)
        lock.release()
        return returnValue
    }

    override fun get(key: K): V? {
        lock.acquire()
        val returnValue = original[key]
        lock.release()
        return returnValue
    }

    override fun isEmpty(): Boolean {
        lock.acquire()
        val returnValue = original.isEmpty()
        lock.release()
        return returnValue
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() {
            lock.acquire()
            val returnValue = original.entries.toSynchronizedSet()
            lock.release()
            return returnValue
        }
    override val keys: MutableSet<K>
        get() {
            lock.acquire()
            val returnValue = original.keys.toSynchronizedSet()
            lock.release()
            return returnValue
        }
    override val values: MutableCollection<V>
        get() {
            lock.acquire()
            val returnValue = original.values.toSynchronizedList()
            lock.release()
            return returnValue
        }

    override fun clear() {
        lock.lock()
        original.clear()
        lock.unlock()
    }

    override fun put(key: K, value: V): V? {
        lock.lock()
        val returnValue = original.put(key, value)
        lock.unlock()
        return returnValue
    }

    override fun putAll(from: Map<out K, V>) {
        lock.lock()
        val returnValue = original.putAll(from)
        lock.unlock()
        return returnValue
    }

    override fun remove(key: K): V? {
        lock.lock()
        val returnValue = original.remove(key)
        lock.unlock()
        return returnValue
    }

    override fun hashCode(): Int {
        lock.acquire()
        val returnValue = original.hashCode()
        lock.release()
        return returnValue
    }

    override fun toString(): String {
        lock.acquire()
        val returnValue = original.toString()
        lock.release()
        return returnValue
    }

    override fun putIfAbsent(key: K, value: V): V? {
        lock.lock()
        val returnValue = original.putIfAbsent(key, value)
        lock.unlock()
        return returnValue
    }

    override fun forEach(action: BiConsumer<in K, in V>) {
        lock.acquire()
        val returnValue = original.forEach(action)
        lock.release()
        return returnValue
    }

    override fun getOrDefault(key: K, defaultValue: V): V {
        lock.acquire()
        val returnValue = original.getOrDefault(key, defaultValue)
        lock.release()
        return returnValue
    }

    fun getOrPut(key: K, defaultValue: () -> V): V {
        lock.lock()
        var value = original[key]
        val returnValue = if (value == null) {
            value = defaultValue()
            original[key] = value
            value
        } else {
            value
        }
        lock.unlock()
        return returnValue
    }

    override fun remove(key: K, value: V): Boolean {
        lock.lock()
        val returnValue = original.remove(key, value)
        lock.unlock()
        return returnValue
    }

    override fun equals(other: Any?): Boolean {
        lock.acquire()
        val returnValue = original == other
        lock.release()
        return returnValue
    }

    override fun replaceAll(function: BiFunction<in K, in V, out V>) {
        lock.lock()
        val returnValue = original.replaceAll(function)
        lock.unlock()
        return returnValue
    }

    override fun compute(key: K, remappingFunction: BiFunction<in K, in V?, out V?>): V? {
        lock.acquire()
        val returnValue = original.compute(key, remappingFunction)
        lock.release()
        return returnValue
    }

    override fun computeIfAbsent(key: K, mappingFunction: Function<in K, out V>): V {
        lock.acquire()
        val returnValue = original.computeIfAbsent(key, mappingFunction)
        lock.release()
        return returnValue
    }

    override fun computeIfPresent(key: K, remappingFunction: BiFunction<in K, in V, out V?>): V? {
        lock.acquire()
        val returnValue = original.computeIfPresent(key, remappingFunction)
        lock.release()
        return returnValue
    }

    override fun replace(key: K, value: V): V? {
        lock.lock()
        val returnValue = original.replace(key, value)
        lock.unlock()
        return returnValue
    }

    override fun merge(key: K, value: V, remappingFunction: BiFunction<in V, in V, out V?>): V? {
        lock.lock()
        val returnValue = original.merge(key, value, remappingFunction)
        lock.unlock()
        return returnValue
    }

    override fun replace(key: K, oldValue: V, newValue: V): Boolean {
        lock.lock()
        val returnValue = original.replace(key, oldValue, newValue)
        lock.unlock()
        return returnValue
    }
}
