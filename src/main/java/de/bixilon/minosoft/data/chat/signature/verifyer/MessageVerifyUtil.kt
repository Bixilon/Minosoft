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

package de.bixilon.minosoft.data.chat.signature.verifyer

import de.bixilon.minosoft.data.chat.signature.ChatSignatureProperties
import de.bixilon.minosoft.data.chat.signature.errors.MessageExpiredError
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.versions.Version
import java.time.Instant
import java.util.*

object MessageVerifyUtil {

    fun checkExpired(sent: Instant, received: Instant, long: Boolean): Exception? {
        val ttl = if (long) ChatSignatureProperties.MESSAGE_TTL_LONG else ChatSignatureProperties.MESSAGE_TTL
        if (received.toEpochMilli() - sent.toEpochMilli() > ttl) {
            return MessageExpiredError(sent, received)
        }
        return null
    }

    @Deprecated("TODO")
    fun verifyMessage(
        version: Version,
        sent: Instant,
        received: Instant,
        versionId: Int,
        seed: Long,
        content: String,
        sender: UUID,
    ): Exception? {
        checkExpired(sent, received, version >= ProtocolVersions.V_22W42A)?.let { return it }

        // TODO: Verify signature

        return null
    }
}
