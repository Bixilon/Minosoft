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

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W41A;

public class PacketAttachEntity extends PlayS2CPacket {
    private final int entityId;
    private final int vehicleId;
    private final boolean leash;

    public PacketAttachEntity(PlayInByteBuffer buffer) {
        this.entityId = buffer.readInt();
        this.vehicleId = buffer.readInt();
        if (buffer.getVersionId() < V_15W41A) {
            this.leash = buffer.readBoolean();
            return;
        }
        this.leash = true;
    }

    @Override
    public void handle(PlayConnection connection) {
        Entity entity = connection.getWorld().getEntities().get(getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.attachTo(getVehicleId());
        // ToDo leash support
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Attaching entity %d to entity %d (leash=%s)", this.entityId, this.vehicleId, this.leash));
    }

    public int getEntityId() {
        return this.entityId;
    }

    public int getVehicleId() {
        return this.vehicleId;
    }

    public boolean isLeash() {
        return this.leash;
    }
}
