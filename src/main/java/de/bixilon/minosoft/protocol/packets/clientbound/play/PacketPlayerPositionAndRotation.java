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
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PlayerPositionAndRotationServerboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.play.TeleportConfirmServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketPlayerPositionAndRotation extends PlayClientboundPacket {
    private final Vec3 position;
    private final EntityRotation rotation;
    private boolean onGround;
    private byte flags;
    private int teleportId;
    private boolean dismountVehicle = true;

    public PacketPlayerPositionAndRotation(PlayInByteBuffer buffer) {
        this.position = buffer.readEntityPosition();
        this.rotation = new EntityRotation(buffer.readFloat(), buffer.readFloat(), 0);
        if (buffer.getVersionId() < V_14W03B) {
            this.onGround = buffer.readBoolean();
            return;
        } else {
            this.flags = buffer.readByte();
        }
        if (buffer.getVersionId() >= V_15W42A) {
            this.teleportId = buffer.readVarInt();
        }
        if (buffer.getVersionId() < V_21W05A) {
            return;
        }
        this.dismountVehicle = buffer.readBoolean();
    }

    @Override
    public void handle(PlayConnection connection) {
        // ToDo: GUI should do this
        connection.getPlayer().getEntity().setPosition(getPosition());
        if (connection.getVersion().getVersionId() >= V_15W42A) {
            connection.sendPacket(new TeleportConfirmServerboundPacket(getTeleportId()));
        } else {
            connection.sendPacket(new PlayerPositionAndRotationServerboundPacket(getPosition(), getRotation(), isOnGround()));
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received player location: (position=%s, rotation=%s, onGround=%b)", this.position, this.rotation, this.onGround));
    }

    public Vec3 getPosition() {
        return this.position;
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
