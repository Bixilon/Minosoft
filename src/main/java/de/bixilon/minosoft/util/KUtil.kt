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

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.ResourceLocationAble
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.TextFormattable
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.collections.LockMap
import de.bixilon.minosoft.util.collections.SynchronizedMap
import de.bixilon.minosoft.util.enum.AliasableEnum
import de.bixilon.minosoft.util.json.Jackson
import glm_.vec2.Vec2t
import glm_.vec3.Vec3t
import glm_.vec4.Vec4t
import sun.misc.Unsafe
import java.io.*
import java.lang.reflect.Field
import java.nio.ByteBuffer
import java.time.Instant
import java.util.*
import kotlin.Pair
import kotlin.random.Random


object KUtil {
    val UNSAFE: Unsafe

    init {
        val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe")
        unsafeField.isAccessible = true
        UNSAFE = unsafeField[null] as Unsafe
    }

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

    @Suppress("UNCHECKED_CAST")
    fun <T> Any?.unsafeCast(): T {
        return this as T
    }

    inline fun <reified T> Any?.nullCast(): T? {
        if (this is T) {
            return this
        }
        return null
    }

    fun Any?.toResourceLocation(): ResourceLocation {
        return when (this) {
            is String -> ResourceLocation(this)
            is ResourceLocation -> this
            else -> TODO("Don't know how to turn $this into a resource location!")
        }
    }

    fun <K, V> lockMapOf(vararg pairs: Pair<K, V>): LockMap<K, V> {
        return LockMap(mutableMapOf(*pairs))
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

    private fun <K> Any.synchronizedCopy(lock: Object? = null, copier: () -> K): K {
        val ret: K
        synchronized(lock ?: this) {
            ret = copier()
        }
        return ret
    }

    fun <K, V> Map<K, V>.toSynchronizedMap(): SynchronizedMap<K, V> {
        return when (this) {
            is LockMap<*, *> -> {
                lock.acquire()
                val map: SynchronizedMap<K, V> = SynchronizedMap(this.toMutableMap()).unsafeCast()
                lock.release()
                map
            }
            is SynchronizedMap<*, *> -> {
                val map: SynchronizedMap<K, V>
                synchronized(this.lock) {
                    map = SynchronizedMap(this.toMutableMap()).unsafeCast()
                }
                map
            }
            else -> synchronizedCopy { SynchronizedMap(this.toMutableMap()) }
        }
    }

    fun <V> Collection<V>.toSynchronizedList(): MutableList<V> {
        return synchronizedCopy { Collections.synchronizedList(this.toMutableList()) }
    }

    fun <V> Collection<V>.toSynchronizedSet(): MutableSet<V> {
        return synchronizedCopy { Collections.synchronizedSet(this.toMutableSet()) }
    }

    fun <T> T.synchronizedDeepCopy(): T {
        return when (this) {
            is Map<*, *> -> {
                val map: MutableMap<Any?, Any?> = synchronizedMapOf()

                for ((key, value) in this) {
                    map[key.synchronizedDeepCopy()] = value.synchronizedDeepCopy()
                }
                map.unsafeCast()
            }
            is List<*> -> {
                val list: MutableList<Any?> = synchronizedListOf()

                for (key in this) {
                    list += key.synchronizedDeepCopy()
                }

                list.unsafeCast()
            }
            is Set<*> -> {
                val set: MutableSet<Any?> = synchronizedSetOf()

                for (key in this) {
                    set += key.synchronizedDeepCopy()
                }

                set.unsafeCast()
            }
            is ItemStack -> this.copy().unsafeCast()
            is ChatComponent -> this
            is String -> this
            is Number -> this
            is Boolean -> this
            null -> null.unsafeCast()
            else -> TODO("Don't know how to copy ${(this as T)!!::class.java.name}")
        }
    }

    fun Set<String>.toResourceLocationList(): Set<ResourceLocation> {
        val ret: MutableSet<ResourceLocation> = mutableSetOf()

        for (resourceLocation in this) {
            ret += resourceLocation.toResourceLocation()
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

    fun <T> Boolean.decide(`true`: T, `false`: () -> T): T {
        return if (this) {
            `true`
        } else {
            `false`()
        }
    }

    fun <T> Boolean.decide(`true`: () -> T, `false`: T): T {
        return if (this) {
            `true`()
        } else {
            `false`
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

    operator fun <T> Array<T>.get(enum: Enum<*>): T {
        return this[enum.ordinal]
    }

    fun <K, V> Map<K, Any>.extend(vararg pairs: Pair<K, Any>): Map<K, V> {
        val map: MutableMap<K, V> = mutableMapOf()

        for ((key, value) in this) {
            map[key] = value.unsafeCast()
        }

        for (pair in pairs) {
            map[pair.first] = pair.second.unsafeCast()
        }
        return map.toMap()
    }

    fun <V> Collection<V>.extend(vararg values: Any): List<V> {
        val list: MutableList<V> = mutableListOf()

        for (value in this) {
            list += value.unsafeCast<V>()
        }

        fun add(value: Any?) {
            when (value) {
                is Collection<*> -> {
                    for (element in value) {
                        add(element)
                    }
                }
                else -> list += value.unsafeCast<V>()
            }
        }

        for (value in values) {
            add(value)
        }
        return list.toList()
    }

    fun Any?.format(): ChatComponent {
        return ChatComponent.of(when (this) {
            is ChatComponent -> return this
            null -> TextComponent("null").color(ChatColors.DARK_RED)
            is TextFormattable -> this.toText()
            is Boolean -> TextComponent(this.toString()).color(this.decide(ChatColors.GREEN, ChatColors.RED))
            is Enum<*> -> {
                val name = this.name
                TextComponent(if (name.length == 1) {
                    name
                } else {
                    name.lowercase()
                }).color(ChatColors.YELLOW)
            }
            is Float -> "§d%.3f".format(this)
            is Double -> "§d%.4f".format(this)
            is Number -> TextComponent(this).color(ChatColors.LIGHT_PURPLE)
            is ResourceLocation -> TextComponent(this.toString()).color(ChatColors.GOLD)
            is ResourceLocationAble -> resourceLocation.format()
            is Vec4t<*> -> "(${this.x.format()} ${this.y.format()} ${this.z.format()} ${this.w.format()})"
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

    fun Any?.asList(): List<Any> {
        return this.unsafeCast()
    }

    fun Any.toJson(beautiful: Boolean = false): String {
        return if (beautiful) {
            Jackson.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this)
        } else {
            Jackson.MAPPER.writeValueAsString(this)
        }
    }

    fun String.fromJson(): Any {
        return Jackson.MAPPER.readValue(this, Jackson.JSON_MAP_TYPE)
    }

    fun Any?.toInt(): Int {
        return when (this) {
            is Int -> this
            is Long -> this.toInt()
            is Number -> this.toInt()
            is String -> Integer.valueOf(this)
            else -> TODO()
        }
    }

    fun Any?.toLong(): Long {
        return when (this) {
            is Long -> this
            is Number -> this.toLong()
            is Int -> this.toLong()
            else -> TODO()
        }
    }

    fun Any?.toDouble(): Double {
        return when (this) {
            is Double -> this
            is Number -> this.toDouble()
            else -> TODO()
        }
    }

    fun Any?.toFloat(): Float {
        return when (this) {
            is Float -> this
            is Number -> this.toFloat()
            else -> TODO()
        }
    }

    fun Any?.toBoolean(): Boolean {
        return when (this) {
            is Boolean -> this
            is Number -> this.toInt() == 0x01
            "true" -> true
            "false" -> false
            else -> TODO("$this is not a boolean!")
        }
    }

    fun <T> tryCatch(vararg exceptions: Class<out Throwable> = arrayOf(), executor: () -> T): T? {
        try {
            return executor()
        } catch (thrown: Throwable) {
            if (exceptions.isEmpty()) {
                // Catch all
                return null
            }
            for (exception in exceptions) {
                if (exception.isAssignableFrom(thrown::class.java)) {
                    return null
                }
            }
            throw thrown
        }
    }

    val Throwable.text: TextComponent
        get() = TextComponent(this::class.java.realName + ": " + this.message).color(ChatColors.DARK_RED)

    fun Throwable.toStackTrace(): String {
        val stringWriter = StringWriter()
        this.printStackTrace(PrintWriter(stringWriter))
        return stringWriter.toString()
    }

    fun Int.thousands(): String {
        return String.format("%,d", this)
    }

    val Class<*>.realName: String
        get() = this.name.removePrefix(this.packageName).removePrefix(".")

    fun UUID.trim(): String {
        return this.toString().replace("-", "")
    }

    fun <T : ResourceLocationAble> List<T>.asResourceLocationMap(): Map<ResourceLocation, T> {
        val ret: MutableMap<ResourceLocation, T> = mutableMapOf()

        for (value in this) {
            ret[value.resourceLocation] = value
        }

        return ret.toMap()
    }

    fun <T> T?.check(message: (() -> Any)? = null): T {
        if (this == null) {
            throw NullPointerException(message?.invoke()?.toString() ?: "Null check failed")
        }
        return this
    }

    fun ByteArray.toBase64(): String {
        return Base64.getEncoder().encodeToString(this)
    }

    fun String?.nullCompare(other: String?): Int? {
        if (this == null || other == null) {
            return null
        }
        this.compareTo(other).let {
            if (it != 0) {
                return it
            }
        }
        return null
    }

    fun Any?.autoType(): Any? {
        if (this == null) {
            return null
        }
        if (this is Number) {
            return this
        }
        val string = this.toString()

        if (string == "true") {
            return true
        }
        if (string == "false") {
            return false
        }

        // ToDo: Optimize
        if (string.matches("\\d+".toRegex())) {
            return string.toInt()
        }

        return string
    }

    fun ByteBuffer.toByteArray(): ByteArray {
        val array = ByteArray(this.remaining())
        this.get(array)
        return array
    }

    val BooleanArray.isTrue: Boolean
        get() {
            for (boolean in this) {
                if (!boolean) {
                    return false
                }
            }
            return true
        }

    val time: Long
        get() = Instant.now().toEpochMilli()

    fun safeSaveToFile(destination: File, content: String) {
        val parent = destination.parentFile
        if (!parent.exists()) {
            parent.mkdirs()
            if (!parent.isDirectory) {
                throw IOException("Could not create folder: ${parent.path}")
            }
        }

        val tempFile = File("${destination.path}.tmp")
        if (tempFile.exists()) {
            if (!tempFile.delete()) {
                throw IOException("Could not delete $tempFile!")
            }
        }
        FileWriter(tempFile).apply {
            write(content)
            close()
        }
        if (destination.exists() && !destination.delete()) {
            throw IOException("Could not delete $destination!")
        }
        if (!tempFile.renameTo(destination)) {
            throw IOException("Could not move $tempFile to $destination!")
        }
    }

    val Locale.fullName: String
        get() = language + "_" + country.ifEmpty { language.uppercase() }
}
