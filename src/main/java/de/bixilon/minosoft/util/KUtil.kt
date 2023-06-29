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

package de.bixilon.minosoft.util

import de.bixilon.jiibles.AnyString
import de.bixilon.jiibles.Table
import de.bixilon.jiibles.TableStyles
import de.bixilon.kotlinglm.GLM
import de.bixilon.kotlinglm.vec2.Vec2t
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3t
import de.bixilon.kotlinglm.vec4.Vec4t
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.synchronizedListOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedSet
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.kutil.primitive.DoubleUtil
import de.bixilon.kutil.primitive.DoubleUtil.matches
import de.bixilon.kutil.primitive.IntUtil.checkInt
import de.bixilon.kutil.reflection.ReflectionUtil.forceInit
import de.bixilon.kutil.reflection.ReflectionUtil.realName
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.kutil.time.Cooldown
import de.bixilon.kutil.url.URLProtocolStreamHandlers
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection
import de.bixilon.minosoft.protocol.network.network.client.netty.NettyClient
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.buffers.OutByteBuffer
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.account.microsoft.MicrosoftOAuthUtils
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.url.ResourceURLHandler
import io.netty.channel.SimpleChannelInboundHandler
import javafx.application.Platform
import org.kamranzafar.jtar.TarHeader
import java.security.SecureRandom
import java.util.*
import javax.net.ssl.SSLContext


object KUtil {
    val NULL_UUID = UUID(0L, 0L)
    val RANDOM = Random()
    val EMPTY_BYTE_ARRAY = ByteArray(0)

    fun bitSetOf(long: Long): BitSet {
        return BitSet.valueOf(longArrayOf(long))
    }

    fun Any?.toResourceLocation(): ResourceLocation {
        return when (this) {
            is String -> ResourceLocation.of(this)
            is ResourceLocation -> this
            else -> throw IllegalArgumentException("Don't know how to turn $this into a resource location!")
        }
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

        return ret
    }

    fun pause() {
        var setBreakPointHere = 1
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

    fun Collection<Int>.entities(connection: PlayConnection): Set<Entity> {
        val entities: MutableList<Entity> = mutableListOf()
        for (id in this) {
            entities += connection.world.entities[id] ?: continue
        }
        return entities.toSet()
    }

    fun Any?.format(): ChatComponent {
        return ChatComponent.of(
            when (this) {
                is ChatComponent -> return this
                is CharSequence -> this.toString()
                null -> TextComponent("null").color(ChatColors.DARK_RED)
                is TextFormattable -> this.toText()
                is Boolean -> TextComponent(this.toString()).color(this.decide(ChatColors.GREEN, ChatColors.RED))
                is Enum<*> -> {
                    val name = this.name
                    TextComponent(
                        if (name.length == 1) {
                            name
                        } else {
                            name.lowercase()
                        }
                    ).color(ChatColors.YELLOW)
                }

                is Float -> "§d%.3f".format(this)
                is Double -> "§d%.4f".format(this)
                is Number -> TextComponent(this).color(ChatColors.LIGHT_PURPLE)
                is ResourceLocation -> TextComponent(this.toString()).color(ChatColors.GOLD)
                is Identified -> identifier.format()
                is Vec4t<*> -> "(${this._x.format()} ${this._y.format()} ${this._z.format()} ${this._w.format()})"
                is Vec3t<*> -> "(${this._x.format()} ${this._y.format()} ${this._z.format()})"
                is Vec2t<*> -> "(${this._x.format()} ${this._y.format()})"
                else -> this.toString()
            }
        )
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

    val Throwable.text: TextComponent
        get() = TextComponent(this::class.java.realName + ": " + this.message).color(ChatColors.DARK_RED)


    fun <T : Identified> List<T>.asResourceLocationMap(): Map<ResourceLocation, T> {
        val ret: MutableMap<ResourceLocation, T> = mutableMapOf()

        for (value in this) {
            ret[value.identifier] = value
        }

        return ret
    }

    fun String?.nullCompare(other: String?): Int? {
        if (this == null || other == null) {
            return null
        }
        val compared = this.compareTo(other)
        if (compared != 0) {
            return compared
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

        string.checkInt()?.let { return it }

        return string
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

    fun TarHeader.generalize() {
        userId = 0
        groupId = 0
        modTime = 0L
        userName = StringBuffer("nobody")
        groupName = StringBuffer("nobody")
    }


    fun initUtilClasses() {
        DefaultThreadPool += { GlobalEventMaster::class.java.forceInit() }
        DefaultThreadPool += { ShutdownManager::class.java.forceInit() }
        DefaultThreadPool += { Jackson::class.java.forceInit() }
        DefaultThreadPool += { URLProtocolStreamHandlers::class.java.forceInit() }
        DefaultThreadPool += { MicrosoftOAuthUtils::class.java.forceInit() }
        DefaultThreadPool += { TaskScheduler::class.java.forceInit() }
        DefaultThreadPool += { SystemInformation::class.java.forceInit() }
        DefaultThreadPool += { StatusConnection::class.java.forceInit() }
        DefaultThreadPool += { PlayConnection::class.java.forceInit() }
        DefaultThreadPool += { NettyClient::class.java.forceInit() }
        DefaultThreadPool += { SimpleChannelInboundHandler::class.java.forceInit() }
        DefaultThreadPool += { SSLContext.getDefault() }
        DefaultThreadPool += { GLM::class.java.forceInit() } // whole glm
    }

    fun ByteArray.withLengthPrefix(): ByteArray {
        val prefixed = OutByteBuffer()
        prefixed.writeByteArray(this)
        return prefixed.toArray()
    }

    fun Cooldown.setTicks(ticks: Int) {
        set(ticks * ProtocolDefinition.TICK_TIME)
    }

    fun init() {
        Table.DEFAULT_STYLE = TableStyles.FANCY
        URLProtocolStreamHandlers.register("resource", ResourceURLHandler)
        ShutdownManager += {
            for (connection in PlayConnection.ACTIVE_CONNECTIONS.toSynchronizedSet()) {
                connection.network.disconnect()
            }
        }
        ShutdownManager += {
            if (!RunConfiguration.DISABLE_EROS) {
                Platform.exit()
            }
        }
    }

    val Any.charCount: Int
        get() {
            if (this is ChatComponent) return this.length
            if (this is CharSequence) return this.length
            return toString().length
        }

    fun secureRandomUUID(): UUID {
        val random = SecureRandom()
        return UUID(random.nextLong(), random.nextLong())
    }

    fun Initializable.startInit() {
        init()
        postInit()
    }


    fun Vec3d.matches(other: Vec3d, margin: Double = DoubleUtil.DEFAULT_MARGIN): Boolean {
        return x.matches(other.x, margin) && y.matches(other.y, margin) && z.matches(other.z, margin)
    }

    fun <T> table(elements: Collection<T>, vararg headers: AnyString, builder: (T) -> Array<Any?>?): Table {
        val table = Table(headers.unsafeCast())

        for (element in elements) {
            table += builder(element) ?: continue
        }

        return table
    }

    @Deprecated("kutil 1.24")
    fun AbstractLatch.waitIfLess(value: Int, timeout: Long = 0L) = synchronized(notify) {
        while (this.count < value) {
            waitForChange(timeout)
        }
    }

    @Deprecated("kutil 1.24")
    @JvmStatic
    inline fun <reified T : Enum<T>> ValuesEnum<T>.set(): EnumSet<T> {
        return EnumSetUtil.create(T::class.java, VALUES)
    }
}
