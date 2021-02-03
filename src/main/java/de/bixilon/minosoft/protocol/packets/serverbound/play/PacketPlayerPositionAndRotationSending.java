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

import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W06B;

public class PacketPlayerPositionAndRotationSending implements ServerboundPacket {
    private final Location location;
    private final EntityRotation rotation;
    private final boolean onGround;

    public PacketPlayerPositionAndRotationSending(Location location, EntityRotation rotation, boolean onGround) {
        this.location = location;
        this.rotation = rotation;
        this.onGround = onGround;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_PLAYER_POSITION_AND_ROTATION);
        buffer.writeDouble(this.location.getX());
        buffer.writeDouble(this.location.getY());
        if (buffer.getVersionId() < V_14W06B) {
            buffer.writeDouble(this.location.getY() - 1.62);
        }
        buffer.writeDouble(this.location.getZ());
        buffer.writeFloat(this.rotation.getYaw());
        buffer.writeFloat(this.rotation.getPitch());
        buffer.writeBoolean(this.onGround);
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending player position and rotation: (location=%s, rotation=%s, onGround=%b)", this.location, this.rotation, this.onGround));
    }
}
