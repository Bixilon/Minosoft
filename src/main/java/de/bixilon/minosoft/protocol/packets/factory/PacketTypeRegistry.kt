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

package de.bixilon.minosoft.protocol.packets.factory

import com.google.common.reflect.ClassPath
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.reflection.ReflectionUtil.realName
import de.bixilon.kutil.string.StringUtil.toSnakeCase
import de.bixilon.minosoft.protocol.PacketErrorHandler
import de.bixilon.minosoft.protocol.packets.Packet
import de.bixilon.minosoft.protocol.packets.PacketsRoot
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.packets.factory.factories.PacketFactory
import de.bixilon.minosoft.protocol.packets.factory.factories.ReflectionFactory
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.util.KUtil.kClass
import kotlin.reflect.full.companionObjectInstance

object PacketTypeRegistry {
    private var initialized = false
    private val S2C_CLASS_MAP: MutableMap<Class<out S2CPacket>, S2CPacketType> = mutableMapOf()
    private val S2C_STATE_MAP: MutableMap<ProtocolStates, MutableMap<String, S2CPacketType>> = mutableMapOf()
    private val C2S_CLASS_MAP: MutableMap<Class<out C2SPacket>, C2SPacketType> = mutableMapOf()
    private val C2S_STATE_MAP: MutableMap<ProtocolStates, MutableMap<String, C2SPacketType>> = mutableMapOf()


    fun getS2C(clazz: Class<out S2CPacket>): S2CPacketType? {
        return S2C_CLASS_MAP[getPacketMapClass(clazz)]
    }

    fun getS2C(state: ProtocolStates, name: String): S2CPacketType? {
        return S2C_STATE_MAP[state]?.get(name)
    }

    fun getC2S(clazz: Class<out C2SPacket>): C2SPacketType? {
        return C2S_CLASS_MAP[getPacketMapClass(clazz)]
    }

    fun getC2S(state: ProtocolStates, name: String): C2SPacketType? {
        return C2S_STATE_MAP[state]?.get(name)
    }

    private fun getPacketMapClass(clazz: Class<out Packet>): Class<out Packet>? {
        if (!clazz.isAnnotationPresent(LoadPacket::class.java)) {
            return null
        }
        val annotation = clazz.getAnnotation(LoadPacket::class.java)

        if (annotation.parent) {
            val parent = clazz.superclass.unsafeCast<Class<out Any>>()
            if (parent == Packet::class.java || parent == S2CPacket::class.java || parent == C2SPacket::class.java) {
                return null
            }
            return parent.unsafeCast()
        }
        return clazz
    }


    @Suppress("UnstableApiUsage")
    fun init() {
        val classLoader = Thread.currentThread().contextClassLoader
        for (info in ClassPath.from(classLoader).getTopLevelClassesRecursive(PacketsRoot::class.java.packageName)) {
            val clazz = info.load()
            if (!clazz.isAnnotationPresent(LoadPacket::class.java)) {
                continue
            }
            if (!Packet::class.java.isAssignableFrom(clazz) && !PacketFactory::class.java.isAssignableFrom(clazz)) {
                continue
            }
            val kClass = clazz.kClass
            val objectInstance: Any? = kClass.objectInstance ?: kClass.companionObjectInstance
            val annotation = clazz.getAnnotation(LoadPacket::class.java)
            val errorHandler = if (objectInstance is PacketErrorHandler) objectInstance else null

            val direction = when {
                objectInstance is PacketFactory -> objectInstance.direction
                C2SPacket::class.java.isAssignableFrom(clazz) -> PacketDirection.CLIENT_TO_SERVER
                S2CPacket::class.java.isAssignableFrom(clazz) -> PacketDirection.SERVER_TO_CLIENT
                else -> throw IllegalArgumentException("${clazz.realName}: Can not determinate direction!")
            }

            val factory = when {
                direction == PacketDirection.CLIENT_TO_SERVER -> null // They have data constructors only (yet)
                objectInstance is PacketFactory -> objectInstance
                else -> ReflectionFactory(clazz.unsafeCast<Class<Packet>>(), direction, annotation.state)
            }


            var name = annotation.name
            if (name.isBlank()) {
                name = clazz.simpleName.removePrefix("Base").removeSuffix("S2CP").removeSuffix("C2SP").removeSuffix("C2SF").removeSuffix("S2CF").toSnakeCase()
            }

            val mapClass = getPacketMapClass(clazz.unsafeCast()) // ToDo

            if (direction == PacketDirection.CLIENT_TO_SERVER) {
                val c2sClass = clazz.unsafeCast<Class<out C2SPacket>>()
                val type = C2SPacketType(annotation.state, c2sClass, annotation)
                C2S_STATE_MAP.getOrPut(annotation.state) { mutableMapOf() }.put(name, type)?.let { throw IllegalStateException("Packet already mapped: $it (name=$name)") }
                C2S_CLASS_MAP[c2sClass] = type
            } else {
                val s2cClass = clazz.unsafeCast<Class<out S2CPacket>>()
                val type = S2CPacketType(annotation.state, s2cClass, errorHandler, annotation, factory)
                S2C_STATE_MAP.getOrPut(annotation.state) { mutableMapOf() }.put(name, type)?.let { throw IllegalStateException("Packet already mapped: $it (name=$name)") }
                S2C_CLASS_MAP[s2cClass] = type
            }


            println(name)
        }

        initialized = true
    }
}
