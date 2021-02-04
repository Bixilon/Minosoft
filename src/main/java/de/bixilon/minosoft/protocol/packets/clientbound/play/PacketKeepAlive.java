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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketKeepAliveResponse;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W31A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_12_2_PRE2;

public class PacketKeepAlive extends ClientboundPacket {
    private long id;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < V_14W31A) {
            this.id = buffer.readInt();
            return true;
        }
        if (buffer.getVersionId() < V_1_12_2_PRE2) {
            this.id = buffer.readVarInt();
            return true;
        }
        this.id = buffer.readLong();
        return true;
    }

    @Override
    public void handle(Connection connection) {
        connection.sendPacket(new PacketKeepAliveResponse(getId()));
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Keep alive packet received (%d)", this.id));
    }

    public long getId() {
        return this.id;
    }
}
