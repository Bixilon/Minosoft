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

package de.bixilon.minosoft.protocol

import de.bixilon.kutil.base64.Base64Util.toBase64
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.LongUtil.toLong
import de.bixilon.minosoft.protocol.protocol.encryption.CryptManager
import de.bixilon.minosoft.util.KUtil.fromBase64
import java.security.PublicKey
import java.time.Instant

class PlayerPublicKey(
    val expiresAt: Instant,
    val publicKey: PublicKey,
    val signature: ByteArray,
) {

    constructor(nbt: JsonObject) : this(Instant.ofEpochMilli(nbt["expires_at"].toLong()), CryptManager.getPlayerPublicKey(nbt["key"].toString()), nbt["signature"].toString().fromBase64())

    fun toNbt(): JsonObject {
        return mapOf(
            "expires_at" to expiresAt.epochSecond,
            "key" to publicKey.encoded.toBase64(),
            "signature" to signature.toBase64(),
        )
    }
}
