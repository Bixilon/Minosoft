/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.login;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.Util;

import java.util.UUID;

public class PacketLoginSuccess extends ClientboundPacket {
    UUID uuid;
    String username;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < 707) {
            this.uuid = Util.getUUIDFromString(buffer.readString());
            this.username = buffer.readString();
            return true;
        }
        this.uuid = buffer.readUUID();
        this.username = buffer.readString();
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Receiving login success packet (username=%s, UUID=%s)", this.username, this.uuid));
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }
}
