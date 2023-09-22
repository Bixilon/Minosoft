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

package de.bixilon.minosoft.protocol.packets.registry

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.observer.ObserveUtil.jClass
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.PacketNotFoundException
import de.bixilon.minosoft.protocol.packets.registry.factory.PacketFactory
import de.bixilon.minosoft.protocol.packets.registry.factory.PlayPacketFactory
import de.bixilon.minosoft.protocol.packets.types.Packet
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass

open class PacketRegistry(
    val threadSafe: Boolean = true,
    val extra: PacketExtraHandler? = null,
) {
    private val name: MutableMap<String, PacketType> = hashMapOf()
    private val clazz: MutableMap<KClass<out Packet>, PacketType> = hashMapOf()


    fun register(name: String, clazz: KClass<out Packet>? = null, factory: PacketFactory? = null, threadSafe: Boolean = this.threadSafe, lowPriority: Boolean = false, extra: PacketExtraHandler? = this.extra): PacketRegistry {
        /*
        3 cases:
         - packet not registered before -> create packet type
         - packet registered before (loop all parent classes and get packet type, store with current klass)
         - packet is registered before (set factory)
         */
        val nameType = this.name[name]
        val type = clazz?.let { register(clazz) { nameType ?: PacketType(name, threadSafe, lowPriority, extra, null) } } ?: PacketType(name, threadSafe, lowPriority, extra, null)

        if (factory != null) {
            if (type.factory != null) throw IllegalStateException("Ambiguous packet factory: $name")
            type.factory = factory
        }

        if (nameType == null) {
            this.name[name] = type
        }

        return this
    }

    fun registerPlay(name: String, factory: PlayPacketFactory? = null, clazz: KClass<out Packet>? = null, threadSafe: Boolean = this.threadSafe, lowPriority: Boolean = false, extra: PacketExtraHandler? = this.extra): PacketRegistry {
        return register(name, clazz, factory, threadSafe, lowPriority, extra)
    }

    private fun register(clazz: KClass<*>, type: () -> PacketType): PacketType {
        if (clazz == Any::class) return type.invoke()
        this.clazz[clazz]?.let { return it }

        val superclass = Reflection.createKotlinClass(clazz.jClass.superclass)
        val type = register(superclass, type)
        this.clazz[clazz.unsafeCast()] = type

        return type
    }

    operator fun get(clazz: KClass<out Packet>): PacketType {
        return this.clazz[clazz] ?: throw PacketNotFoundException(clazz)
    }

    operator fun get(name: String): PacketType? {
        return this.name[name]
    }
}
