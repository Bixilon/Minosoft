/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketAttachEntity implements ClientboundPacket {
    int entityId;
    int vehicleId;
    boolean leash;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getProtocolId() < 77) {
            this.entityId = buffer.readInt();
            this.vehicleId = buffer.readInt();
            this.leash = buffer.readBoolean();
            return true;
        }
        this.entityId = buffer.readInt();
        this.vehicleId = buffer.readInt();
        this.leash = true;
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Attaching entity %d to entity %d (leash=%s)", entityId, vehicleId, leash));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public int getEntityId() {
        return entityId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public boolean isLeash() {
        return leash;
    }
}
