/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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
import de.bixilon.jiibles.AnyString
import de.bixilon.jiibles.Table
import de.bixilon.jiibles.TableStyles
import glm_.vec2.Vec2t
import glm_.vec3.Vec3d
import glm_.vec3.Vec3t
import glm_.vec4.Vec4t
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.synchronizedListOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedSet
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.ForcePooledRunnable
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.kutil.primitive.DoubleUtil
import de.bixilon.kutil.primitive.DoubleUtil.matches
import de.bixilon.kutil.primitive.IntUtil.isIntSafe
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.ReflectionUtil.forceInit
import de.bixilon.kutil.reflection.ReflectionUtil.getUnsafeField
import de.bixilon.kutil.reflection.ReflectionUtil.realName
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.kutil.url.URLProtocolStreamHandlers
import de.bixilon.minosoft.config.profile.manager.ProfileManagers
import de.bixilon.minosoft.data.container.stack.ItemStack
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
import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.protocol.network.network.client.netty.NettyClient
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.status.StatusSession
import de.bixilon.minosoft.protocol.packets.registry.DefaultPackets
import de.bixilon.minosoft.protocol.protocol.DefaultPacketMapping
import de.bixilon.minosoft.protocol.protocol.buffers.OutByteBuffer
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.recipes.RecipeFactories
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.account.microsoft.MicrosoftOAuthUtils
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.url.ResourceURLHandler
import glm_.glm
import io.netty.channel.SimpleChannelInboundHandler
import javafx.application.Platform
import org.kamranzafar.jtar.TarHeader
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import javax.net.ssl.SSLContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.ExperimentalTime


object KUtil {
    private val OBJECT_NODE_CHILDREN = ObjectNode::class.java.getUnsafeField("_children").field
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

    fun pause() {
        var setBreakPointHere = 1
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
    inline fun Float.format() = TextComponent("§d%.3f".format(this)).color(ChatColors.LIGHT_PURPLE)
    inline fun Double.format() = TextComponent("§d%.4f".format(this)).color(ChatColors.LIGHT_PURPLE)
    inline fun Number.format() = TextComponent(this.toString()).color(ChatColors.LIGHT_PURPLE)

    fun ResourceLocation.format() = TextComponent(this.toString()).color(ChatColors.GOLD)

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
        if (this == null) return null
        if (this is Number) return this

        val string = this.toString()

        if (string == "true") return true
        if (string == "false") return false

        string.isIntSafe()?.let { return it }

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


    fun initBootClasses() {
        DefaultThreadPool += ForcePooledRunnable { GlobalEventMaster::class.java.forceInit() }
        DefaultThreadPool += ForcePooledRunnable { ShutdownManager::class.java.forceInit() }

        for (manager in ProfileManagers) {
            DefaultThreadPool += { manager.init() }
        }
        DefaultThreadPool += ForcePooledRunnable { URLProtocolStreamHandlers::class.java.forceInit() }
        DefaultThreadPool += ForcePooledRunnable { MicrosoftOAuthUtils::class.java.forceInit() }
        DefaultThreadPool += ForcePooledRunnable { TaskScheduler::class.java.forceInit() }
        DefaultThreadPool += ForcePooledRunnable { SystemInformation::class.java.forceInit() }
        DefaultThreadPool += ForcePooledRunnable { StatusSession::class.java.forceInit() }
        DefaultThreadPool += ForcePooledRunnable { NettyClient::class.java.forceInit() }
        DefaultThreadPool += ForcePooledRunnable { SimpleChannelInboundHandler::class.java.forceInit() }
        DefaultThreadPool += ForcePooledRunnable { SSLContext.getDefault() }
        DefaultThreadPool += ForcePooledRunnable { DefaultPackets::class.java.forceInit() }
        DefaultThreadPool += ForcePooledRunnable { DefaultPacketMapping::class.java.forceInit() }

    }


    fun initPlayClasses() {
        DefaultThreadPool += { PlaySession::class.java.forceInit() }
        DefaultThreadPool += { glm::class.java.forceInit() } // whole glm
        DefaultThreadPool += { ItemFactories::class.java.forceInit() }
        DefaultThreadPool += { BlockFactories::class.java.forceInit() }
        DefaultThreadPool += { RecipeFactories::class.java.forceInit() }
        DefaultThreadPool += { IntegratedStatusEffects::class.java.forceInit() }
    }

    fun ByteArray.withLengthPrefix(): ByteArray {
        val prefixed = OutByteBuffer()
        prefixed.writeByteArray(this)
        return prefixed.toArray()
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

    @Deprecated("jiibles 1.2")
    fun <T> table(elements: Collection<T>, vararg headers: AnyString, builder: (T) -> Array<Any?>?): Table {
        val table = Table(headers.unsafeCast())

        for (element in elements) {
            table += builder(element) ?: continue
        }

        return table
    }

    fun PlayInByteBuffer.dump(name: String) {
        val pointer = pointer
        this.pointer = 0
        val data = readRemaining()
        this.pointer = pointer

        val path = "/home/moritz/${name}_${Versions.getById(this.versionId)?.name?.replace(".", "_")}.bin"
        val stream = FileOutputStream(path)
        stream.write(data)
        stream.close()
        println("Packet dumped to $path")
    }

    fun ObjectNode.toMap(): HashMap<String, JsonNode> = OBJECT_NODE_CHILDREN[this]

    @Deprecated("kutil 1.27.2")
    val TIME_ZERO = now() // TODO

    @Deprecated("kutil 1.27.2")
    operator fun Duration.rem(rem: Duration): Duration {
        if (rem == Duration.ZERO) throw ArithmeticException("Division by zero!")

        return (inWholeNanoseconds % rem.inWholeNanoseconds).nanoseconds
    }

    @Deprecated("kutil 1.27.2")
    fun SimpleDateFormat.format1(instant: Instant) = format(instant.toEpochMilli())

    @OptIn(ExperimentalTime::class)
    @Deprecated("kutil 1.27.2")
    fun SimpleDateFormat.format1(instant: kotlin.time.Instant) = format(instant.toEpochMilliseconds())


    @Deprecated("kutil 1.27.2")
    fun String.toPath() = Path.of(this)

    @Deprecated("kutil 1.27.2")
    operator fun Path.div(file: String) = this.resolve(file)

    @Deprecated("kutil 1.27.2")
    operator fun Path.div(file: Path) = this.resolve(file)

    @Deprecated("kutil 1.27.2")
    operator fun File.div(file: String) = this.resolve(file)

    @Deprecated("kutil 1.27.2")
    operator fun File.div(file: File) = this.resolve(file)
}
