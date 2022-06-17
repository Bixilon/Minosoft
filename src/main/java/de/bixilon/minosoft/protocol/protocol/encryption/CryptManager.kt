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
package de.bixilon.minosoft.protocol.protocol.encryption

import de.bixilon.minosoft.util.KUtil.fromBase64
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object CryptManager {
    // little thanks to https://skmedix.github.io/ForgeJavaDocs/javadoc/forge/1.7.10-10.13.4.1614/net/minecraft/util/CryptManager.html

    fun createNewSharedKey(): SecretKey {
        val key = KeyGenerator.getInstance("AES")
        key.init(128)
        return key.generateKey()
    }

    fun getServerHash(serverId: String, publicKey: PublicKey, secretKey: SecretKey): ByteArray {
        return digestOperation(serverId.toByteArray(StandardCharsets.ISO_8859_1), secretKey.encoded, publicKey.encoded)
    }

    private fun digestOperation(vararg bytes: ByteArray?): ByteArray {
        val digest = MessageDigest.getInstance("SHA-1")
        for (b in bytes) {
            digest.update(b)
        }
        return digest.digest()
    }

    fun decodePublicKey(key: ByteArray): PublicKey {
        val keySpec = X509EncodedKeySpec(key)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    fun encryptData(key: Key, data: ByteArray): ByteArray {
        return cipherOperation(Cipher.ENCRYPT_MODE, key, data)
    }

    private fun cipherOperation(opMode: Int, key: Key, data: ByteArray): ByteArray {
        return createTheCipherInstance(opMode, key.algorithm, key).doFinal(data)
    }

    private fun createTheCipherInstance(opMode: Int, transformation: String, key: Key): Cipher {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(opMode, key)
        return cipher
    }

    fun createNetCipherInstance(opMode: Int, key: Key): Cipher {
        val cipher = Cipher.getInstance("AES/CFB8/NoPadding")
        cipher.init(opMode, key, IvParameterSpec(key.encoded))
        return cipher
    }

    fun getPlayerPrivateKey(key: String): PrivateKey {
        return getPlayerPrivateKey(key.replace("\n", "").removePrefix("-----BEGIN RSA PRIVATE KEY-----").removeSuffix("-----END RSA PRIVATE KEY-----").fromBase64())
    }

    fun getPlayerPrivateKey(key: ByteArray): PrivateKey {
        val rsa = KeyFactory.getInstance("RSA")
        return rsa.generatePrivate(PKCS8EncodedKeySpec(key))
    }

    fun getPlayerPublicKey(key: String): PublicKey {
        return getPlayerPublicKey(key.replace("\n", "").removePrefix("-----BEGIN RSA PUBLIC KEY-----").removeSuffix("-----END RSA PUBLIC KEY-----").fromBase64())
    }

    fun getPlayerPublicKey(key: ByteArray): PublicKey {
        val rsa = KeyFactory.getInstance("RSA")
        return rsa.generatePublic(X509EncodedKeySpec(key))
    }
}
