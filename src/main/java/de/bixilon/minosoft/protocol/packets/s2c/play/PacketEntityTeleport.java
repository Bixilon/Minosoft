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

package de.bixilon.minosoft.protocol.packets.s2c.play;

import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W25B;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_16W06A;

public class PacketEntityTeleport extends PlayS2CPacket {
    private final int entityId;
    private final Vec3 position;
    private final int yaw;
    private final int pitch;
    private boolean onGround;

    public PacketEntityTeleport(PlayInByteBuffer buffer) {
        this.entityId = buffer.readEntityId();

        if (buffer.getVersionId() < V_16W06A) {
            this.position = new Vec3(buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt(), buffer.readFixedPointNumberInt());
        } else {
            this.position = buffer.readPosition();
        }
        this.yaw = buffer.readAngle();
        this.pitch = buffer.readAngle();

        if (buffer.getVersionId() >= V_14W25B) {
            this.onGround = buffer.readBoolean();
        }
    }

    @Override
    public void handle(PlayConnection connection) {
        Entity entity = connection.getWorld().getEntity(getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.setPosition(getRelativePosition());
        entity.setRotation(getYaw(), getPitch());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Entity %d moved to %s (yaw=%s, pitch=%s)", this.entityId, this.position, this.yaw, this.pitch));
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Vec3 getRelativePosition() {
        return this.position;
    }

    public int getYaw() {
        return this.yaw;
    }

    public int getPitch() {
        return this.pitch;
    }
}
