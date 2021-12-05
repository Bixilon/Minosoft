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
package de.bixilon.minosoft.protocol.packets.s2c.login

import de.bixilon.minosoft.protocol.ErrorHandler
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.login.EncryptionResponseC2SP
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.CryptManager
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toBase64
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.math.BigInteger

class EncryptionRequestS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val serverId: String = buffer.readString()
    val publicKey: ByteArray = buffer.readByteArray()
    val verifyToken: ByteArray = buffer.readByteArray()

    override fun handle(connection: PlayConnection) {
        val secretKey = CryptManager.createNewSharedKey()
        val publicKey = CryptManager.decodePublicKey(publicKey)
        val serverHash = BigInteger(CryptManager.getServerHash(serverId, publicKey, secretKey)).toString(16)
        connection.account.join(serverHash)
        connection.sendPacket(EncryptionResponseC2SP(secretKey, verifyToken, publicKey))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Encryption request (serverId=$serverId, publicKey=${publicKey.toBase64()}, verifyToken=${verifyToken.toBase64()})" }
    }

    companion object : ErrorHandler {
        override fun onError(connection: Connection) {
            connection.disconnect()
        }
    }
}
