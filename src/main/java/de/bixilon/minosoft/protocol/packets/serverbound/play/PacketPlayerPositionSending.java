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

import de.bixilon.minosoft.data.entities.Position;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W06B;

public class PacketPlayerPositionSending implements ServerboundPacket {
    private final Position position;
    private final boolean onGround;

    public PacketPlayerPositionSending(Position position, boolean onGround) {
        this.position = position;
        this.onGround = onGround;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_PLAYER_POSITION);
        buffer.writeDouble(this.position.getX());
        buffer.writeDouble(this.position.getY());
        if (buffer.getVersionId() < V_14W06B) {
            buffer.writeDouble(0.0); // TODo
        }
        buffer.writeDouble(this.position.getZ());
        buffer.writeBoolean(this.onGround);
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending player position (position=%s)", this.position));
    }
}
