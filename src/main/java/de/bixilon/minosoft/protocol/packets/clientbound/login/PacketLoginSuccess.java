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
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.UUID;

public class PacketLoginSuccess implements ClientboundPacket {
    UUID uuid;
    String username;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersion().getVersionNumber() < ProtocolVersion.VERSION_1_16_2.getVersionNumber()) {
            uuid = UUID.fromString(buffer.readString());
            username = buffer.readString();
            return true;
        }
        uuid = buffer.readUUID();
        username = buffer.readString();
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Receiving login success packet (username: %s, UUID: %s)", username, uuid));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }
}
