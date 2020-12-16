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

import de.bixilon.minosoft.data.entities.RelativeLocation;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

import static de.bixilon.minosoft.protocol.protocol.Versions.V_14W25B;
import static de.bixilon.minosoft.protocol.protocol.Versions.V_16W06A;

public class PacketEntityMovement extends ClientboundPacket {
    int entityId;
    RelativeLocation location;
    boolean onGround;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.entityId = buffer.readEntityId();
        if (buffer.getVersionId() < V_16W06A) {
            this.location = new RelativeLocation(buffer.readFixedPointNumberByte(), buffer.readFixedPointNumberByte(), buffer.readFixedPointNumberByte());
        } else {
            this.location = new RelativeLocation(buffer.readShort() / 4096F, buffer.readShort() / 4096F, buffer.readShort() / 4096F); // / 128 / 32
        }
        if (buffer.getVersionId() >= V_14W25B) {
            this.onGround = buffer.readBoolean();
        }
        return true;
    }

    @Override
    public void handle(Connection connection) {
        Entity entity = connection.getPlayer().getWorld().getEntity(getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.setLocation(getRelativeLocation());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Entity %d moved relative %s", this.entityId, this.location));
    }

    public int getEntityId() {
        return this.entityId;
    }

    public RelativeLocation getRelativeLocation() {
        return this.location;
    }
}
