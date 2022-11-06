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
package de.bixilon.minosoft.protocol.packets.c2s.login

import de.bixilon.kutil.base64.Base64Util.toBase64
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.encryption.EncryptionSignatureData
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket(state = ProtocolStates.LOGIN)
class EncryptionC2SP private constructor(
    val secret: ByteArray,
    val nonce: Any,
) : PlayC2SPacket {

    constructor(secret: ByteArray, nonce: ByteArray) : this(secret, nonce as Any)
    constructor(secret: ByteArray, nonce: EncryptionSignatureData) : this(secret, nonce as Any)

    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writeByteArray(secret)
        if (buffer.versionId < ProtocolVersions.V_22W17A || buffer.versionId >= ProtocolVersions.V_22W43A) {
            buffer.writeByteArray(nonce.nullCast<ByteArray>() ?: throw IllegalArgumentException("In nonce must be a bytearray!"))
        } else {
            if (nonce is ByteArray) {
                buffer.writeBoolean(true)
                buffer.writeByteArray(nonce)
            } else {
                buffer.writeBoolean(false)
                val nonce = nonce as EncryptionSignatureData
                buffer.writeLong(nonce.salt)
                buffer.writeByteArray(nonce.signature)
            }
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Encryption response (secret=${secret.toBase64()}, nonce=$nonce)" }
    }
}
