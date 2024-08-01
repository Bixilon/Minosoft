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
package de.bixilon.minosoft.protocol.packets.s2c.login

import com.google.common.primitives.Longs
import de.bixilon.kutil.base64.Base64Util.toBase64
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.c2s.login.EncryptionC2SP
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.encryption.CryptManager
import de.bixilon.minosoft.protocol.protocol.encryption.EncryptionSignatureData
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.math.BigInteger
import java.security.SecureRandom
import javax.crypto.Cipher

class EncryptionS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val serverId: String = buffer.readString()
    val publicKey: ByteArray = buffer.readByteArray()
    val nonce: ByteArray = buffer.readByteArray()

    override fun handle(session: PlaySession) {
        if (!session.account.supportsEncryption && !session.profiles.account.ignoreNotEncryptedAccount) {
            throw IllegalAccessError("Account does not support encryption, but the server requested it!\nMaybe you try to join with an offline account on an online server?")
        }

        val secretKey = CryptManager.createNewSharedKey()
        val publicKey = CryptManager.decodePublicKey(publicKey)
        val serverHash = BigInteger(CryptManager.getServerHash(serverId, publicKey, secretKey)).toString(16)
        session.account.join(serverHash)

        val encryptCipher = CryptManager.createNetCipherInstance(Cipher.ENCRYPT_MODE, secretKey)
        val decryptCipher = CryptManager.createNetCipherInstance(Cipher.DECRYPT_MODE, secretKey)

        val encryptedSecretKey = CryptManager.encryptData(publicKey, secretKey.encoded)
        val privateKey = session.player.keyManagement.key
        if (session.version.requiresSignedLogin && privateKey != null) {
            val salt = SecureRandom().nextLong()

            val signature = CryptManager.createSignature(session.version)
            signature.initSign(privateKey.private)
            signature.update(nonce)
            signature.update(Longs.toByteArray(salt))
            val signed = signature.sign()

            session.network.send(EncryptionC2SP(encryptedSecretKey, EncryptionSignatureData(salt, signed)))
        } else {
            session.network.send(EncryptionC2SP(encryptedSecretKey, CryptManager.encryptData(publicKey, nonce)))
        }

        session.network.setupEncryption(encryptCipher, decryptCipher)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Encryption request (serverId=$serverId, publicKey=${publicKey.toBase64()}, nonce=${nonce.toBase64()})" }
    }
}
