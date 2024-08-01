/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.network.client.netty.packet.receiver

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.ForcePooledRunnable
import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.protocol.network.network.client.ClientNetwork
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.NetworkException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.PacketHandleException
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.registry.PacketType
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket

class PacketReceiver(
    val network: ClientNetwork,
    val session: Session,
) {
    private val listener: MutableList<C2SPacketListener> = ArrayList()
    private val queue: MutableList<QueuedS2CP<*>> = ArrayList(0)
    var paused = false
        set(value) {
            if (field == value) return
            if (!value) process() // try to keep order
            field = value
            if (!value) process() // flush again, so packets that might got queued won't get lost
        }


    private fun handle(packet: S2CPacket) {
        val profile = if (session is PlaySession) session.profiles.other else OtherProfileManager.selected
        val reduced = profile.log.reducedProtocolLog
        packet.log(reduced)
        packet.handle(session)
    }

    private fun handleError(type: PacketType, error: Throwable) {
        if (type.extra != null) {
            type.extra.onError(error, session)
        }
        network.handleError(error)
    }

    private fun tryHandle(type: PacketType, packet: S2CPacket) {
        if (!network.connected) return

        try {
            handle(packet)
        } catch (exception: NetworkException) {
            handleError(type, exception)
        } catch (error: Throwable) {
            handleError(type, PacketHandleException(error))
        }
    }

    private fun PacketType.handleAsync(): Boolean {
        if (!threadSafe) return false
        if (lowPriority) return true
        if (DefaultThreadPool.queueSize >= (DefaultThreadPool.threadCount - 1)) return false // might backlog a bit
        return true
    }

    private fun tryHandle2(type: PacketType, packet: S2CPacket) {
        if (type.handleAsync()) {
            DefaultThreadPool += ForcePooledRunnable(priority = if (type.lowPriority) ThreadPool.NORMAL else ThreadPool.HIGH) { tryHandle(type, packet) }
        } else {
            tryHandle(type, packet)
        }
    }

    private fun notify(packet: S2CPacket): Boolean {
        if (listener.isEmpty()) return false
        for (listener in listener) {
            val discard = ignoreAll { listener.onReceive(packet) } ?: continue
            if (discard) return true
        }
        return false
    }

    fun onReceive(type: PacketType, packet: S2CPacket) {
        if (network.detached) return
        if (!network.connected) return
        val discard = notify(packet)
        if (discard) return

        if (paused) {
            queue += QueuedS2CP(type, packet)
        } else {
            tryHandle2(type, packet)
        }
    }

    private fun process() {
        while (queue.isNotEmpty()) {
            val (type, packet) = queue.removeFirst()
            tryHandle(type, packet)
        }
    }

    operator fun plusAssign(listener: C2SPacketListener) = listen(listener)

    fun listen(listener: C2SPacketListener) {
        this.listener += listener
    }
}
