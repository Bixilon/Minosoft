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

package de.bixilon.minosoft.protocol.packets.s2c.login;

import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.c2s.login.EncryptionResponseC2SP;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.CryptManager;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.mojang.api.exceptions.MojangJoinServerErrorException;
import de.bixilon.minosoft.util.mojang.api.exceptions.NoNetworkConnectionException;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;

public class PacketEncryptionRequest extends PlayS2CPacket {
    private final String serverId; // normally empty
    private final byte[] publicKey;
    private final byte[] verifyToken;

    public PacketEncryptionRequest(PlayInByteBuffer buffer) {
        this.serverId = buffer.readString();
        this.publicKey = buffer.readByteArray();
        this.verifyToken = buffer.readByteArray();
    }

    @Override
    public void handle(PlayConnection connection) {
        SecretKey secretKey = CryptManager.createNewSharedKey();
        PublicKey publicKey = CryptManager.decodePublicKey(getPublicKey());
        String serverHash = new BigInteger(CryptManager.getServerHash(getServerId(), publicKey, secretKey)).toString(16);
        try {
            connection.getAccount().join(serverHash);
        } catch (MojangJoinServerErrorException | NoNetworkConnectionException e) {
            e.printStackTrace();
            connection.disconnect();
            return;
        }
        connection.sendPacket(new EncryptionResponseC2SP(secretKey, getVerifyToken(), publicKey));
    }

    @Override
    public void log() {
        Log.protocol("[IN] Receiving encryption request packet");
    }

    public byte[] getPublicKey() {
        return this.publicKey;
    }

    public byte[] getVerifyToken() {
        return this.verifyToken;
    }

    public String getServerId() {
        return this.serverId;
    }
}
