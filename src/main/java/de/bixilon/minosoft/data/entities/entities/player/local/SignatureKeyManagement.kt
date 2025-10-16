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

package de.bixilon.minosoft.data.entities.entities.player.local

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.concurrent.lock.RWLock
import de.bixilon.kutil.concurrent.schedule.TaskScheduler.runLater
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.time.Interval
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.chat.message.internal.InternalChatMessage
import de.bixilon.minosoft.data.chat.signature.ChatSignatureProperties
import de.bixilon.minosoft.data.chat.signature.errors.KeyExpiredError
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageEvent
import de.bixilon.minosoft.protocol.network.NetworkConnection
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.c2s.play.SessionDataC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.account.minecraft.MinecraftPrivateKey
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SignatureKeyManagement(
    val session: PlaySession,
    val account: Account,
) {
    private val lock = RWLock.rwlock()

    var key: PlayerPrivateKey? = null
        private set

    fun init(latch: AbstractLatch?) {
        if (key != null) throw IllegalStateException("Already initialized!")
        fetchKey(latch)
    }

    private fun registerRefresh(interval: Interval) {
        runLater(interval) {
            val connection = session.connection.nullCast<NetworkConnection>() ?: return@runLater
            if (session.error != null || (session.established && !connection.active) || (connection.active && !connection.client!!.encrypted)) {
                // session is dead
                return@runLater
            }
            try {
                fetchKey(null)
            } catch (error: Throwable) {
                session.events.fire(ChatMessageEvent(session, InternalChatMessage(TextComponent("Failed to refresh private key. Trying again in 60s: $error"))))
                registerRefresh(60.seconds)
            }
        }
    }

    private fun updateKey(key: MinecraftPrivateKey) {
        key.requireSignature(account.uuid)
        this.key = PlayerPrivateKey(
            expiresAt = key.expiresAt,
            signature = key.getSignature(session.version.versionId),
            private = key.pair.private,
            public = key.pair.public,
        )
        val refreshInterval = maxOf((key.refreshedAfter.toEpochMilli() - System.currentTimeMillis()).milliseconds, 100.milliseconds) // TODO: kotlin instant
        registerRefresh(refreshInterval)
        sendSession()
    }

    private fun fetchKey(latch: AbstractLatch?) {
        lock.lock()
        try {
            account.fetchKey(latch)?.let { updateKey(it) }
        } finally {
            lock.unlock()
        }
    }


    fun acquire() = lock.acquire()
    fun release() = lock.release()

    fun sendSession() {
        val key = key?.playerKey ?: return
        if (session.version.versionId < ProtocolVersions.V_22W43A) return
        if (session.connection !is NetworkConnection) return
        if (session.connection.state != ProtocolStates.PLAY) return
        if (!session.connection.client!!.encrypted) {
            return
        }
        session.connection.send(SessionDataC2SP(session.sessionId, key))
    }

    companion object {

        fun verify(key: PlayerPrivateKey, time: Instant) {
            if (time.isAfter(key.expiresAt.minusMillis(ChatSignatureProperties.MINIMUM_KEY_TTL.toLong()))) {
                throw KeyExpiredError(key)
            }
        }
    }
}
