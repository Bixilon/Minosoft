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
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.account.minecraft.key.MinecraftKeyPair
import de.bixilon.minosoft.util.yggdrasil.YggdrasilUtil
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*

data class MinecraftPrivateKey(
    @JsonProperty("keyPair") val pair: MinecraftKeyPair,
    @JsonProperty("publicKeySignature") val signature: String?,
    @JsonProperty("publicKeySignatureV2") val signatureV2: String?,
    @JsonProperty("expiresAt") val expiresAt: Instant,
    @JsonProperty("refreshedAfter") val refreshedAfter: Instant,
) {
    @get:JsonIgnore val signatureBytes: ByteArray? by lazy { signature?.fromBase64() }
    @get:JsonIgnore val signatureBytesV2: ByteArray by lazy { signatureV2!!.fromBase64() }

    @JsonIgnore
    fun isExpired(): Boolean {
        val now = Instant.now()
        return now.isAfter(expiresAt) || now.isAfter(refreshedAfter)
    }

    @JsonIgnore
    fun requireSignature(uuid: UUID) {
        MinecraftKeyPair.requireSignature(uuid, expiresAt, pair.public, signatureBytesV2)
        signatureBytes?.let { YggdrasilUtil.requireSignature((expiresAt.toEpochMilli().toString() + pair.publicString).toByteArray(StandardCharsets.US_ASCII), it) }
    }

    fun getSignature(versionId: Int): ByteArray {
        if (versionId < ProtocolVersions.V_1_19_1_PRE4) {
            return signatureBytes ?: throw IllegalStateException("v1 signature required")
        }
        return signatureBytesV2
    }
}
