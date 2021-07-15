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
package de.bixilon.minosoft.protocol.protocol

import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object CryptManager {
    // little thanks to https://skmedix.github.io/ForgeJavaDocs/javadoc/forge/1.7.10-10.13.4.1614/net/minecraft/util/CryptManager.html

    @JvmStatic
    fun createNewSharedKey(): SecretKey {
        val key = KeyGenerator.getInstance("AES")
        key.init(128)
        return key.generateKey()
    }

    @JvmStatic
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

    @JvmStatic
    fun decodePublicKey(key: ByteArray): PublicKey {
        val keySpec = X509EncodedKeySpec(key)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    @JvmStatic
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

    @JvmStatic
    fun createNetCipherInstance(opMode: Int, key: Key): Cipher {
        val cipher = Cipher.getInstance("AES/CFB8/NoPadding")
        cipher.init(opMode, key, IvParameterSpec(key.encoded))
        return cipher
    }
}
