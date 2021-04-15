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

import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketSetPassenger extends PlayS2CPacket {
    private final int vehicleId;
    private final int[] entityIds;


    public PacketSetPassenger(PlayInByteBuffer buffer) {
        this.vehicleId = buffer.readVarInt();
        this.entityIds = new int[buffer.readVarInt()];
        for (int i = 0; i < this.entityIds.length; i++) {
            this.entityIds[i] = buffer.readVarInt();
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Attaching %d entities (vehicleId=%d)", this.entityIds.length, this.vehicleId));
    }

    public int getVehicleId() {
        return this.vehicleId;
    }

    public int[] getEntityIds() {
        return this.entityIds;
    }
}
