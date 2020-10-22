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

import de.bixilon.minosoft.data.mappings.Item;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketCollectItem implements ClientboundPacket {
    Item item;
    int collectorId;
    int count;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < 7) {
            item = buffer.getConnection().getMapping().getItemById(buffer.readInt());
            collectorId = buffer.readInt();
            return true;
        }
        item = buffer.getConnection().getMapping().getItemById(buffer.readVarInt());
        collectorId = buffer.readVarInt();
        if (buffer.getVersionId() >= 301) {
            count = buffer.readVarInt();
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Item %s was collected by %d (count=%s)", item, collectorId, ((count == 0) ? "?" : count)));
    }

    public Item getItem() {
        return item;
    }

    public int getCollectorId() {
        return collectorId;
    }

    public int getCount() {
        return count;
    }
}
