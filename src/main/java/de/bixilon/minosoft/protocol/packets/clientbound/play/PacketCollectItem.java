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

public class PacketCollectItem implements ClientboundPacket {
    int itemId;
    int collectorId;
    int count;


    @Override
    public boolean read(InByteBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                itemId = buffer.readInt();
                collectorId = buffer.readInt();
                return true;
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
                itemId = buffer.readVarInt();
                collectorId = buffer.readVarInt();
                return true;
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
                itemId = buffer.readVarInt();
                collectorId = buffer.readVarInt();
                count = buffer.readVarInt();
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Item %d was collected by %d (count=%s)", itemId, collectorId, ((count == 0) ? "?" : count)));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public int getCollectorId() {
        return collectorId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getCount() {
        return count;
    }
}
