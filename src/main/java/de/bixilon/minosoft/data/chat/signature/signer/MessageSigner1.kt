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

package de.bixilon.minosoft.data.chat.signature.signer

import com.google.common.primitives.Longs
import de.bixilon.minosoft.data.chat.signature.LastSeenMessageList
import de.bixilon.minosoft.data.chat.signature.signer.MessageSigningUtil.getJsonSignatureBytes
import de.bixilon.minosoft.data.chat.signature.signer.MessageSigningUtil.update
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.protocol.encryption.CryptManager
import de.bixilon.minosoft.protocol.versions.Version
import java.security.PrivateKey
import java.time.Instant
import java.util.*

class MessageSigner1(
    private val version: Version,
) : MessageSigner {

    override fun signMessage(privateKey: PrivateKey, message: String, preview: ChatComponent?, salt: Long, sender: UUID, time: Instant, lastSeen: LastSeenMessageList): ByteArray {
        return signMessage(privateKey, message, salt, sender, time)
    }

    fun signMessage(privateKey: PrivateKey, message: String, salt: Long, sender: UUID, time: Instant): ByteArray {
        val signature = CryptManager.createSignature(version)

        signature.initSign(privateKey)

        signature.update(Longs.toByteArray(salt))
        signature.update(sender)
        signature.update(Longs.toByteArray(time.epochSecond))
        signature.update(message.getJsonSignatureBytes())

        return signature.sign()
    }
}
