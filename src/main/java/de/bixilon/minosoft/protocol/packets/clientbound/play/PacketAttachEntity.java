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
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

import static de.bixilon.minosoft.protocol.protocol.Versions.V_15W41A;

public class PacketAttachEntity extends ClientboundPacket {
    int entityId;
    int vehicleId;
    boolean leash;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.entityId = buffer.readInt();
        this.vehicleId = buffer.readInt();
        if (buffer.getVersionId() < V_15W41A) {
            this.leash = buffer.readBoolean();
            return true;
        }
        this.leash = true;
        return true;
    }

    @Override
    public void handle(Connection connection) {
        Entity entity = connection.getPlayer().getWorld().getEntity(getEntityId());
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
