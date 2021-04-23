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

public class PacketEntityMovement extends PlayS2CPacket {
    private final int entityId;
    private final Vec3 position;
    private boolean onGround;

    public PacketEntityMovement(PlayInByteBuffer buffer) {
        this.entityId = buffer.readEntityId();
        if (buffer.getVersionId() < V_16W06A) {
            this.position = new Vec3(buffer.readFixedPointNumberByte(), buffer.readFixedPointNumberByte(), buffer.readFixedPointNumberByte());
        } else {
            this.position = new Vec3(buffer.readShort() / 4096F, buffer.readShort() / 4096F, buffer.readShort() / 4096F); // / 128 / 32
        }
        if (buffer.getVersionId() >= V_14W25B) {
            this.onGround = buffer.readBoolean();
        }
    }

    @Override
    public void handle(PlayConnection connection) {
        Entity entity = connection.getWorld().getEntities().get(getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.forceMove(getRelativePosition());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Entity %d moved relative %s", this.entityId, this.position));
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Vec3 getRelativePosition() {
        return this.position;
    }
}
