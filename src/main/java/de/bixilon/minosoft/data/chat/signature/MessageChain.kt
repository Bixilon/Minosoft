/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.chat.signature

import com.fasterxml.jackson.core.io.JsonStringEncoder
import com.google.common.hash.Hashing
import com.google.common.primitives.Longs
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.encryption.CryptManager
import java.security.PrivateKey
import java.time.Instant
import java.util.*

class MessageChain {
    private var previous: ByteArray? = null

    fun signMessage(version: Version, privateKey: PrivateKey, message: String, preview: ChatComponent?, salt: Long, sender: UUID, time: Instant, lastSeen: LastSeenMessageList): ByteArray {
        val signature = CryptManager.createSignature(version)

        signature.initSign(privateKey)

        if (version < ProtocolVersions.V_1_19_1_PRE4) {
            signature.update(Longs.toByteArray(salt))
            signature.update(Longs.toByteArray(sender.mostSignificantBits))
            signature.update(Longs.toByteArray(sender.leastSignificantBits))
            signature.update(Longs.toByteArray(time.epochSecond))
            signature.update(message.getSignatureBytes())
        } else {
            val buffer = OutByteBuffer()
            buffer.writeLong(salt)
            buffer.writeLong(time.epochSecond)
            buffer.writeBareByteArray(message.getSignatureBytes())

            if (version.versionId >= ProtocolVersions.V_1_19_1_PRE5) {
                buffer.writeByte(0x46)
                // ToDo: send preview text (optional)
                for (entry in lastSeen.messages) {
                    buffer.writeByte(0x46)
                    buffer.writeUUID(entry.profile)
                    buffer.writeBareByteArray(entry.signature)
                }
            }
            val hash = Hashing.sha256().hashBytes(buffer.toArray()).asBytes()

            previous?.let { signature.update(it) }
            signature.update(Longs.toByteArray(sender.mostSignificantBits))
            signature.update(Longs.toByteArray(sender.leastSignificantBits))
            signature.update(hash)
        }

        val singed = signature.sign()
        this.previous = singed

        return singed
    }

    private fun String.getSignatureBytes(): ByteArray {
        return """{"text":"${String(JsonStringEncoder.getInstance().quoteAsString(this))}"}""".encodeNetwork()
    }
}
