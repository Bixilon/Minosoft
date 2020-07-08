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
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketSetPassenger implements ClientboundPacket {
    int vehicleId;
    int[] entityIds;


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                this.vehicleId = buffer.readVarInt();
                entityIds = new int[buffer.readVarInt()];
                for (int i = 0; i < entityIds.length; i++) {
                    entityIds[i] = buffer.readVarInt();
                }
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Attaching %d entities (vehicleId=%d)", entityIds.length, vehicleId));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public int[] getEntityIds() {
        return entityIds;
    }
}
