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

package de.bixilon.minosoft.util.account.minecraft

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.kutil.base64.Base64Util.fromBase64
import de.bixilon.minosoft.util.YggdrasilUtil
import de.bixilon.minosoft.util.account.minecraft.key.MinecraftKeyPair
import java.nio.charset.StandardCharsets
import java.time.Instant

data class MinecraftPrivateKey(
    @JsonProperty("keyPair") val pair: MinecraftKeyPair,
    @JsonProperty("publicKeySignature") val signature: String,
    @JsonProperty("expiresAt") val expiresAt: Instant,
    @JsonProperty("refreshedAfter") val refreshedAfter: Instant,
) {
    @get:JsonIgnore val signatureBytes: ByteArray by lazy { signature.fromBase64() }

    fun isExpired(): Boolean {
        val now = Instant.now()
        return now.isAfter(expiresAt) || now.isAfter(refreshedAfter)
    }

    fun isSignatureCorrect(): Boolean {
        val bytes = (expiresAt.toEpochMilli().toString() + pair.public).toByteArray(StandardCharsets.US_ASCII)

        return YggdrasilUtil.verify(bytes, signatureBytes)
    }
}
