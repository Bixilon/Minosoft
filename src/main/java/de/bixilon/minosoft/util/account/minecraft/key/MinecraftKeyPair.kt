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

package de.bixilon.minosoft.util.account.minecraft.key

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer
import de.bixilon.minosoft.protocol.protocol.encryption.CryptManager
import de.bixilon.minosoft.util.yggdrasil.YggdrasilException
import de.bixilon.minosoft.util.yggdrasil.YggdrasilUtil
import java.security.PublicKey
import java.time.Instant
import java.util.*

data class MinecraftKeyPair(
    @JsonProperty("privateKey") val privateString: String,
    @JsonProperty("publicKey") val publicString: String,
) {
    @get:JsonIgnore val private by lazy { CryptManager.getPlayerPrivateKey(privateString) }
    @get:JsonIgnore val public by lazy { CryptManager.getPlayerPublicKey(publicString) }

    companion object {

        fun isSignatureCorrect(uuid: UUID, expiresAt: Instant, publicKey: PublicKey, signature: ByteArray): Boolean {
            val signed = OutByteBuffer()
            signed.writeUUID(uuid)
            signed.writeLong(expiresAt.toEpochMilli())
            signed.writeBareByteArray(publicKey.encoded)
            return YggdrasilUtil.verify(signed.toArray(), signature)
        }

        fun requireSignature(uuid: UUID, expiresAt: Instant, publicKey: PublicKey, signature: ByteArray) {
            if (!isSignatureCorrect(uuid, expiresAt, publicKey, signature)) {
                throw YggdrasilException()
            }
        }


    }
}
