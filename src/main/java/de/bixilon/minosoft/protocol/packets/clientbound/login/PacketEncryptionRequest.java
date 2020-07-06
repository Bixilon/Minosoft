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

package de.bixilon.minosoft.protocol.packets.clientbound.login;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketEncryptionRequest implements ClientboundPacket {
    String serverId; //normally empty
    byte[] publicKey;
    byte[] verifyToken;

    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                serverId = buffer.readString();
                publicKey = buffer.readBytes(buffer.readShort()); // read length, then the bytes
                verifyToken = buffer.readBytes(buffer.readShort()); // read length, then the bytes
                return true;
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                serverId = buffer.readString();
                publicKey = buffer.readBytes(buffer.readVarInt()); // read length, then the bytes
                verifyToken = buffer.readBytes(buffer.readVarInt()); // read length, then the bytes
                return true;
        }
        return false;
    }

    @Override
    public void log() {
        Log.protocol("Receiving encryption request packet");
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getVerifyToken() {
        return verifyToken;
    }

    public String getServerId() {
        return serverId;
    }
}
