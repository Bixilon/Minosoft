/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import de.bixilon.jiibles.Table
import de.bixilon.jiibles.TableStyles
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedSet
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.ThreadPoolRunnable
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.kutil.primitive.DoubleUtil
import de.bixilon.kutil.primitive.DoubleUtil.matches
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.ReflectionUtil.forceInit
import de.bixilon.kutil.reflection.ReflectionUtil.getUnsafeField
import de.bixilon.kutil.reflection.ReflectionUtil.realName
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.kutil.url.URLProtocolStreamHandlers
import de.bixilon.minosoft.commands.util.StringReader
import de.bixilon.minosoft.config.profile.manager.ProfileManagers
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.blocks.factory.BlockFactories
import de.bixilon.minosoft.data.registries.effects.IntegratedStatusEffects
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.ItemFactories
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.eros.ErosOptions
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.protocol.network.network.client.netty.NettyClient
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.status.StatusSession
import de.bixilon.minosoft.protocol.packets.registry.DefaultPackets
import de.bixilon.minosoft.protocol.protocol.DefaultPacketMapping
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.recipes.RecipeFactories
import de.bixilon.minosoft.util.account.microsoft.MicrosoftOAuthUtils
import de.bixilon.minosoft.util.url.ResourceURLHandler
import io.netty.channel.SimpleChannelInboundHandler
import javafx.application.Platform
import java.io.FileOutputStream
import java.security.SecureRandom
import java.util.*
import javax.net.ssl.SSLContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds


object KUtil {
    private val OBJECT_NODE_CHILDREN = ObjectNode::class.java.getUnsafeField("_children").field
    val NULL_UUID = UUID(0L, 0L)
    val EMPTY_BYTE_ARRAY = ByteArray(0)

    fun bitSetOf(long: Long): BitSet {
        return BitSet.valueOf(longArrayOf(long))
    }

    fun Any?.toResourceLocation() = when (this) {
        is String -> ResourceLocation.of(this)
        is ResourceLocation -> this
        else -> throw IllegalArgumentException("Don't know how to turn $this into a resource location!")
    }

    @Deprecated("sheet")
    fun <T> T.synchronizedDeepCopy(): T {
        return when (this) {
            is Map<*, *> -> {
                val map: MutableMap<Any?, Any?> = synchronizedMapOf()

                for ((key, value) in this) {
                    map[key.synchronizedDeepCopy()] = value.synchronizedDeepCopy()
                }
                map.unsafeCast()
            }

            is Set<*> -> {
                val set: MutableSet<Any?> = synchronizedSetOf()

                for (key in this) {
                    set += key.synchronizedDeepCopy()
                }

                set.unsafeCast()
            }

            null -> null.unsafeCast()
            else -> TODO("Don't know how to copy ${(this as T)!!::class.java.name}")
        }
    }

    fun Collection<Int>.entities(session: PlaySession): Set<Entity> {
        val entities: MutableList<Entity> = mutableListOf()
        for (id in this) {
            entities += session.world.entities[id] ?: continue
        }
        return entities.toSet()
    }

    fun ChatComponent.format() = this
    fun CharSequence.format() = ChatComponent.of(this.toString())
    fun TextFormattable.format() = ChatComponent.of(this.toText())
    fun Identified.format() = identifier.format()
    fun Boolean.format() = if (this) TextFormattable.TRUE else TextFormattable.FALSE
    inline fun Float.format() = TextComponent("%.3f".format(this)).color(ChatColors.LIGHT_PURPLE)
    inline fun Double.format() = TextComponent("%.4f".format(this)).color(ChatColors.LIGHT_PURPLE)
    inline fun Number.format() = TextComponent(this.toString()).color(ChatColors.LIGHT_PURPLE)

    fun ResourceLocation.format() = TextComponent(this.toString()).color(ChatColors.GOLD)

    @JvmName("formatNull")
    fun Any?.format(): ChatComponent {
        if (this == null) return TextFormattable.NULL

        return this.format()
    }

    fun Any.format() = when (this) {
        is ChatComponent -> this.format()
        is CharSequence -> this.format()
        is TextFormattable -> this.format()
        is Float -> this.format()
        is Double -> this.format()
        is Number -> this.format()
        is Boolean -> this.format()
        is ResourceLocation -> this.format()
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

        is Identified -> identifier.format()
        else -> ChatComponent.of(this.toString())
    }

    val Throwable.text: TextComponent
        get() = TextComponent(this::class.java.realName + ": " + this.message).color(ChatColors.DARK_RED)


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


    fun initBootClasses() {
        DefaultThreadPool += ThreadPoolRunnable(forcePool = true) { GlobalEventMaster::class.java.forceInit() }
        DefaultThreadPool += ThreadPoolRunnable(forcePool = true) { ShutdownManager::class.java.forceInit() }

        for (manager in ProfileManagers) {
            DefaultThreadPool += { manager.init() }
        }
        DefaultThreadPool += ThreadPoolRunnable(forcePool = true) { URLProtocolStreamHandlers::class.java.forceInit() }
        DefaultThreadPool += ThreadPoolRunnable(forcePool = true) { MicrosoftOAuthUtils::class.java.forceInit() }
        DefaultThreadPool += ThreadPoolRunnable(forcePool = true) { TaskScheduler::class.java.forceInit() }
        DefaultThreadPool += ThreadPoolRunnable(forcePool = true) { SystemInformation::class.java.forceInit() }
        DefaultThreadPool += ThreadPoolRunnable(forcePool = true) { StatusSession::class.java.forceInit() }
        DefaultThreadPool += ThreadPoolRunnable(forcePool = true) { NettyClient::class.java.forceInit() }
        DefaultThreadPool += ThreadPoolRunnable(forcePool = true) { SimpleChannelInboundHandler::class.java.forceInit() }
        DefaultThreadPool += ThreadPoolRunnable(forcePool = true) { SSLContext.getDefault() }
        DefaultThreadPool += ThreadPoolRunnable(forcePool = true) { DefaultPackets::class.java.forceInit() }
        DefaultThreadPool += ThreadPoolRunnable(forcePool = true) { DefaultPacketMapping::class.java.forceInit() }

    }


    fun initPlayClasses() {
        DefaultThreadPool += { PlaySession::class.java.forceInit() }
        DefaultThreadPool += { ItemFactories::class.java.forceInit() }
        DefaultThreadPool += { BlockFactories::class.java.forceInit() }
        DefaultThreadPool += { RecipeFactories::class.java.forceInit() }
        DefaultThreadPool += { IntegratedStatusEffects::class.java.forceInit() }
    }

    fun init() {
        Table.DEFAULT_STYLE = TableStyles.FANCY
        URLProtocolStreamHandlers.register("resource", ResourceURLHandler)
        ShutdownManager += {
            for (session in PlaySession.ACTIVE_CONNECTIONS.toSynchronizedSet()) {
                session.terminate()
            }
        }
        ShutdownManager += {
            if (!ErosOptions.disabled) {
                Platform.exit()
            }
        }
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

    fun PlayInByteBuffer.dump(name: String) {
        val offset = this@dump.offset
        this.offset = data.offset
        val data = readRemaining()
        this.offset = offset

        val path = "/home/moritz/${name}_${Versions.getById(this.versionId)?.name?.replace(".", "_")}.bin"
        val stream = FileOutputStream(path)
        stream.write(data)
        stream.close()
        println("Packet dumped to $path")
    }

    fun ObjectNode.toMap(): HashMap<String, JsonNode> = OBJECT_NODE_CHILDREN[this]


    fun String.toDuration(): Duration {
        val reader = StringReader(this)
        val value = reader.readNumeric(true, true)!!.toDouble()
        reader.skipWhitespaces()
        return when (val unit = reader.readRest()) {
            "d" -> value.days
            "h" -> value.hours
            "m" -> value.minutes
            "s", "", null -> value.seconds
            "ms" -> value.milliseconds
            "ns" -> value.nanoseconds
            else -> throw IllegalArgumentException("Unexpected time unit: $unit (value: $value)")
        }
    }
}
