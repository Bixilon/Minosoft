/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.serverbound.login;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.CryptManager;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import javax.crypto.SecretKey;
import java.security.PublicKey;

public class PacketEncryptionResponse implements ServerboundPacket {

    final byte[] secret;
    final byte[] token;
    final SecretKey secretKey;


    public PacketEncryptionResponse(SecretKey secret, byte[] token, PublicKey key) {
        this.secretKey = secret;
        this.secret = CryptManager.encryptData(key, secret.getEncoded());
        this.token = CryptManager.encryptData(key, token);
        log();
    }


    public SecretKey getSecretKey() {
        return secretKey;
    }

    @Override
    public OutPacketBuffer write(ProtocolVersion version) {
        OutPacketBuffer buffer = new OutPacketBuffer(version, version.getPacketCommand(Packets.Serverbound.LOGIN_ENCRYPTION_RESPONSE));
        switch (version) {
            case VERSION_1_7_10:
                buffer.writeShort((short) secret.length);
                buffer.writeBytes(secret);
                buffer.writeShort((short) token.length);
                buffer.writeBytes(token);
                break;
            case VERSION_1_8:
            case VERSION_1_9_4:
                buffer.writeVarInt(secret.length);
                buffer.writeBytes(secret);
                buffer.writeVarInt(token.length);
                buffer.writeBytes(token);
                break;
        }
        //buffer.writeString(username);
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol("Sending encryption response");
    }
}
