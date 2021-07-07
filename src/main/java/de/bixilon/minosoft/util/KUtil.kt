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

package de.bixilon.minosoft.util

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonWriter
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextFormattable
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.collections.SynchronizedMap
import de.bixilon.minosoft.util.enum.AliasableEnum
import de.bixilon.minosoft.util.json.JSONSerializer
import glm_.vec2.Vec2t
import glm_.vec3.Vec3t
import okio.Buffer
import sun.misc.Unsafe
import java.lang.reflect.Field
import java.util.*
import kotlin.Pair
import kotlin.random.Random

object KUtil {

    fun <T : Enum<*>> getEnumValues(values: Array<T>): Map<String, T> {
        val ret: MutableMap<String, T> = mutableMapOf()

        for (value in values) {
            ret[value.name.lowercase(Locale.getDefault())] = value

            if (value is AliasableEnum) {
                for (name in value.names) {
                    ret[name] = value
                }
            }
        }

        return ret.toMap()
    }

    fun bitSetOf(long: Long): BitSet {
        return BitSet.valueOf(longArrayOf(long))
    }

    fun <T> Any.unsafeCast(): T {
        return this as T
    }

    inline fun <reified T> Any.nullCast(): T? {
        if (this is T) {
            return this
        }
        return null
    }

    fun String.asResourceLocation(): ResourceLocation {
        return ResourceLocation(this)
    }

    fun <K, V> synchronizedMapOf(vararg pairs: Pair<K, V>): SynchronizedMap<K, V> {
        return SynchronizedMap(mutableMapOf(*pairs))
    }

    fun <V> synchronizedSetOf(vararg values: V): MutableSet<V> {
        return Collections.synchronizedSet(mutableSetOf(*values))
    }

    fun <V> synchronizedListOf(vararg values: V): MutableList<V> {
        return Collections.synchronizedList(mutableListOf(*values))
    }

    private fun <K> Any.synchronizedCopy(copier: () -> K): K {
        val ret: K
        synchronized(this) {
            ret = copier()
        }
        return ret
    }

    fun <K, V> Map<K, V>.toSynchronizedMap(): SynchronizedMap<K, V> {
        return synchronizedCopy { SynchronizedMap(this.toMutableMap()) }
    }

    fun <V> Collection<V>.toSynchronizedList(): MutableList<V> {
        return synchronizedCopy { Collections.synchronizedList(this.toMutableList()) }
    }

    fun <V> Collection<V>.toSynchronizedSet(): MutableSet<V> {
        return synchronizedCopy { Collections.synchronizedSet(this.toMutableSet()) }
    }

    fun Set<String>.toResourceLocationList(): Set<ResourceLocation> {
        val ret: MutableSet<ResourceLocation> = mutableSetOf()

        for (resourceLocation in this) {
            ret += resourceLocation.asResourceLocation()
        }

        return ret.toSet()
    }

    fun pause() {
        var setBreakPointHere = 1
    }

    fun hardCrash() {
        val field = Unsafe::class.java.getDeclaredField("theUnsafe")
        field.isAccessible = true
        val unsafe = field[null] as Unsafe
        unsafe.putAddress(0, 0)
    }

    fun Random.nextFloat(min: Float = Float.MIN_VALUE, max: Float = Float.MAX_VALUE): Float {
        return min + this.nextFloat() * (max - min)
    }

    /**
     * Converts millis to ticks
     */
    val Number.ticks: Int
        get() = this.toInt() / ProtocolDefinition.TICK_TIME

    /**
     * Converts ticks to millis
     */
    val Number.millis: Int
        get() = this.toInt() * ProtocolDefinition.TICK_TIME

    fun Random.chance(intPercent: Int): Boolean {
        return this.nextInt(100) < intPercent
    }

    fun <T> Boolean.decide(`true`: T, `false`: T): T {
        return if (this) {
            `true`
        } else {
            `false`
        }
    }

    fun <T> Boolean.decide(`true`: () -> T, `false`: () -> T): T {
        return if (this) {
            `true`()
        } else {
            `false`()
        }
    }

    fun String.asUUID(): UUID {
        return Util.getUUIDFromString(this)
    }

    fun Collection<Int>.entities(connection: PlayConnection): Set<Entity> {
        val entities: MutableList<Entity> = mutableListOf()
        for (id in this) {
            entities += connection.world.entities[id] ?: continue
        }
        return entities.toSet()
    }

    operator fun <T> List<T>.get(enum: Enum<*>): T {
        return this[enum.ordinal]
    }

    fun <K, V> Map<K, Any>.extend(vararg pairs: Pair<K, Any>): Map<K, V> {
        val map: MutableMap<K, V> = mutableMapOf()

        for ((key, value) in this) {
            map[key] = value as V
        }

        for (pair in pairs) {
            map[pair.first] = pair.second as V
        }
        return map.toMap()
    }

    fun <V> Collection<Any>.extend(vararg values: Any): List<V> {
        val list: MutableList<V> = mutableListOf()

        for (value in this) {
            list += value as V
        }

        for (value in values) {
            list += value as V
        }
        return list.toList()
    }

    fun Any?.format(): ChatComponent {
        return ChatComponent.of(when (this) {
            null -> "§4null"
            is TextFormattable -> this.toText()
            is Boolean -> {
                if (this) {
                    "§atrue"
                } else {
                    "§cfalse"
                }
            }
            is Enum<*> -> {
                val name = this.name
                "§e" + if (name.length == 1) {
                    name
                } else {
                    name.lowercase()
                }
            }
            is Float -> "§d%.3f".format(this)
            is Double -> "§d%.4f".format(this)
            is Number -> {
                "§d$this"
            }
            is Vec3t<*> -> "(${this.x.format()} ${this.y.format()} ${this.z.format()})"
            is Vec2t<*> -> "(${this.x.format()} ${this.y.format()})"
            else -> this.toString()
        })
    }


    fun Field.setValue(instance: Any, value: Any?) {
        this.isAccessible = true

        // ToDo
        // if (Modifier.isFinal(this.modifiers)) {
        //     FieldUtils.removeFinalModifier(this)
        // }

        this.set(instance, value)
    }


    fun Any.mapCast(): Map<Any, Any>? {
        return this.nullCast()
    }

    fun Any.listCast(): Collection<Any>? {
        return this.nullCast()
    }

    fun Any.toJson(beautiful: Boolean = false, adapter: JsonAdapter<Any> = JSONSerializer.ANY_ADAPTER): String {
        val buffer = Buffer()
        val jsonWriter: JsonWriter = JsonWriter.of(buffer)
        if (beautiful) {
            jsonWriter.indent = "  "
        }
        synchronized(this) {
            adapter.toJson(jsonWriter, this)
        }
        return buffer.readUtf8()
    }

    fun String.fromJson(): Any {
        return JSONSerializer.ANY_ADAPTER.fromJson(this)!!
    }

    fun Any.toInt(): Int {
        return when (this) {
            is Int -> this
            is Number -> this.toInt()
            is String -> Integer.valueOf(this)
            is Long -> this.toInt()
            else -> TODO()
        }
    }
}
