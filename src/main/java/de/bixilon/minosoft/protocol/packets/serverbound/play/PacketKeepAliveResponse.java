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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

import static de.bixilon.minosoft.protocol.protocol.Versions.V_14W31A;
import static de.bixilon.minosoft.protocol.protocol.Versions.V_1_12_2_PRE2;

public class PacketKeepAliveResponse implements ServerboundPacket {

    final long id;

    public PacketKeepAliveResponse(long id) {
        this.id = id;
    }

    public PacketKeepAliveResponse(int id) {
        this.id = id;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_KEEP_ALIVE);
        if (buffer.getVersionId() < V_14W31A) {
            buffer.writeInt((int) this.id);
        } else if (buffer.getVersionId() < V_1_12_2_PRE2) {
            buffer.writeVarInt((int) this.id);
        } else {
            buffer.writeLong(this.id);
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending keep alive back (%d)", this.id));
    }
}
