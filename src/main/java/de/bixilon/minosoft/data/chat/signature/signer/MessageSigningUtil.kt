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

package de.bixilon.minosoft.data.chat.signature.signer

import com.fasterxml.jackson.core.io.JsonStringEncoder
import com.google.common.primitives.Longs
import de.bixilon.minosoft.protocol.ProtocolUtil.encodeNetwork
import java.security.Signature
import java.util.*

object MessageSigningUtil {

    fun String.getSignatureBytes(): ByteArray {
        return """{"text":"${String(JsonStringEncoder.getInstance().quoteAsString(this))}"}""".encodeNetwork()
    }

    fun Signature.update(uuid: UUID) {
        update(Longs.toByteArray(uuid.mostSignificantBits))
        update(Longs.toByteArray(uuid.leastSignificantBits))
    }
}
