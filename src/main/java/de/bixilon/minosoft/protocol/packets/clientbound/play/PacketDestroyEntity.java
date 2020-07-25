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

import java.util.Arrays;


public class PacketDestroyEntity implements ClientboundPacket {
    int[] entityIds;

    @Override
    public boolean read(InByteBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                this.entityIds = new int[buffer.readByte()];
                for (int i = 0; i < entityIds.length; i++) {
                    entityIds[i] = buffer.readInt();
                }
                return true;
            default:
                this.entityIds = new int[buffer.readVarInt()];
                for (int i = 0; i < entityIds.length; i++) {
                    entityIds[i] = buffer.readVarInt();
                }
                return true;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Despawning the following entities: %s", Arrays.toString(entityIds)));
    }

    public int[] getEntityIds() {
        return entityIds;
    }


    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
