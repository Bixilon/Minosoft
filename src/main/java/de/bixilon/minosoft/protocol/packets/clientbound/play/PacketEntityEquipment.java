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

import de.bixilon.minosoft.game.datatypes.inventory.InventorySlots;
import de.bixilon.minosoft.game.datatypes.inventory.Slot;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketEntityEquipment implements ClientboundPacket {
    int entityId;
    InventorySlots.EntityInventory slot;
    Slot data;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                entityId = buffer.readInteger();
                this.slot = InventorySlots.EntityInventory.byId(buffer.readShort());
                this.data = buffer.readSlot(v);
                break;
        }
    }

    @Override
    public void log() {
        if (data != null) {
            Log.protocol(String.format("Entity equipment changed (entityId=%d, slot=%s): %dx %s", entityId, slot.name(), data.getItemCount(), data.getDisplayName()));
        } else {
            // null means nothing, means air
            Log.protocol(String.format("Entity equipment changed (entityId=%d, slot=%s): AIR", entityId, slot.name()));
        }
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public int getEntityId() {
        return entityId;
    }

    public InventorySlots.EntityInventory getSlot() {
        return slot;
    }

    public Slot getData() {
        return data;
    }
}
