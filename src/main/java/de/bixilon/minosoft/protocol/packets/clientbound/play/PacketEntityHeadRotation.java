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

import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketEntityHeadRotation extends ClientboundPacket {
    private final int entityId;
    private final short headYaw;

    public PacketEntityHeadRotation(InByteBuffer buffer) {
        this.entityId = buffer.readEntityId();

        this.headYaw = buffer.readAngle();
    }

    @Override
    public void handle(Connection connection) {
        Entity entity = connection.getWorld().getEntity(getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.setHeadRotation(getHeadYaw());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Entity %d moved head (yaw=%s)", this.entityId, this.headYaw));
    }

    public int getEntityId() {
        return this.entityId;
    }

    public short getHeadYaw() {
        return this.headYaw;
    }
}
