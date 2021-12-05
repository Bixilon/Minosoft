/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.c2s.login

import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.CryptManager
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.util.KUtil.toBase64
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.security.PublicKey
import javax.crypto.SecretKey

class EncryptionResponseC2SP(
    @Deprecated(message = "This is used for enabling encryption in the socket. But it should not be part of the packet") val secretKey: SecretKey,
    val secret: ByteArray,
    val token: ByteArray,
) : PlayC2SPacket {

    constructor(secretKey: SecretKey, token: ByteArray, key: PublicKey) : this(secretKey, CryptManager.encryptData(key, secretKey.encoded), CryptManager.encryptData(key, token))

    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writeByteArray(secret)
        buffer.writeByteArray(token)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Encryption response (secret=${secret.toBase64()}, token=${token.toBase64()})" }
    }
}
