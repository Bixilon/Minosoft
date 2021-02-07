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

import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketConfirmTeleport;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketPlayerPositionAndRotationSending;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W03B;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W42A;

public class PacketPlayerPositionAndRotation extends ClientboundPacket {
    Location location;
    EntityRotation rotation;
    boolean onGround;
    byte flags;

    int teleportId;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.location = buffer.readLocation();
        this.rotation = new EntityRotation(buffer.readFloat(), buffer.readFloat(), 0);
        if (buffer.getVersionId() < V_14W03B) {
            this.onGround = buffer.readBoolean();
            return true;
        } else {
            this.flags = buffer.readByte();
        }
        if (buffer.getVersionId() >= V_15W42A) {
            this.teleportId = buffer.readVarInt();
        }
        return true;
    }

    @Override
    public void handle(Connection connection) {
        // ToDo: GUI should do this
        connection.getPlayer().getEntity().setLocation(getLocation());
        if (connection.getVersion().getVersionId() >= V_15W42A) {
            connection.sendPacket(new PacketConfirmTeleport(getTeleportId()));
        } else {
            connection.sendPacket(new PacketPlayerPositionAndRotationSending(getLocation(), getRotation(), isOnGround()));
        }
        connection.getRenderer().teleport(this.location);
        connection.getRenderer().rotate(this.rotation);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received player location: (location=%s, rotation=%s, onGround=%b)", this.location, this.rotation, this.onGround));
    }

    public Location getLocation() {
        return this.location;
    }

    public EntityRotation getRotation() {
        return this.rotation;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public int getTeleportId() {
        return this.teleportId;
    }

}
