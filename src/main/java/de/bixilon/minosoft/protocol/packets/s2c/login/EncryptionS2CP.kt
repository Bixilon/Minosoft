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
package de.bixilon.minosoft.protocol.packets.s2c.login

import com.google.common.primitives.Longs
import de.bixilon.kutil.base64.Base64Util.toBase64
import de.bixilon.minosoft.protocol.PacketErrorHandler
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.login.EncryptionC2SP
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.encryption.CryptManager
import de.bixilon.minosoft.protocol.protocol.encryption.EncryptionSignatureData
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.math.BigInteger
import java.security.SecureRandom
import javax.crypto.Cipher

@LoadPacket(state = ProtocolStates.LOGIN, threadSafe = false)
class EncryptionS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val serverId: String = buffer.readString()
    val publicKey: ByteArray = buffer.readByteArray()
    val nonce: ByteArray = buffer.readByteArray()

    override fun handle(connection: PlayConnection) {
        val secretKey = CryptManager.createNewSharedKey()
        val publicKey = CryptManager.decodePublicKey(publicKey)
        val serverHash = BigInteger(CryptManager.getServerHash(serverId, publicKey, secretKey)).toString(16)
        connection.account.join(serverHash)

        val encryptCipher = CryptManager.createNetCipherInstance(Cipher.ENCRYPT_MODE, secretKey)
        val decryptCipher = CryptManager.createNetCipherInstance(Cipher.DECRYPT_MODE, secretKey)

        val encryptedSecretKey = CryptManager.encryptData(publicKey, secretKey.encoded)
        val privateKey = connection.player.keyManagement.key
        if (connection.version.requiresSignedLogin && privateKey != null) {
            val salt = SecureRandom().nextLong()

            val signature = CryptManager.createSignature(connection.version)
            signature.initSign(privateKey.private)
            signature.update(nonce)
            signature.update(Longs.toByteArray(salt))
            val signed = signature.sign()

            connection.sendPacket(EncryptionC2SP(encryptedSecretKey, EncryptionSignatureData(salt, signed)))
        } else {
            connection.sendPacket(EncryptionC2SP(encryptedSecretKey, CryptManager.encryptData(publicKey, nonce)))
        }

        connection.network.setupEncryption(encryptCipher, decryptCipher)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Encryption request (serverId=$serverId, publicKey=${publicKey.toBase64()}, nonce=${nonce.toBase64()})" }
    }

    companion object : PacketErrorHandler {
        override fun onError(error: Throwable, connection: Connection) {
            connection.error = error
            connection.network.disconnect()
        }
    }
}
