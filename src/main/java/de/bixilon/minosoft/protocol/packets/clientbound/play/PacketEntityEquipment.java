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
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.util.HashMap;
import java.util.Map;

public class PacketEntityEquipment implements ClientboundPacket {
    int entityId;
    HashMap<InventorySlots.EntityInventory, Slot> slots = new HashMap<>();

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getProtocolId() < 7) {
            entityId = buffer.readInt();
            slots.put(InventorySlots.EntityInventory.byId(buffer.readShort(), buffer.getProtocolId()), buffer.readSlot());
            return true;
        }
        if (buffer.getProtocolId() < 49) {
            entityId = buffer.readVarInt();
            slots.put(InventorySlots.EntityInventory.byId(buffer.readShort(), buffer.getProtocolId()), buffer.readSlot());
            return true;
        }
        if (buffer.getProtocolId() < 743) { //ToDo: find out version
            entityId = buffer.readVarInt();
            slots.put(InventorySlots.EntityInventory.byId(buffer.readVarInt(), buffer.getProtocolId()), buffer.readSlot());
            return true;
        }
        entityId = buffer.readVarInt();
        boolean slotAvailable = true;
        while (slotAvailable) {
            int slotId = buffer.readByte();
            if (slotId >= 0) {
                slotAvailable = false;
            }
            slotId &= 0x7F;
            slots.put(InventorySlots.EntityInventory.byId(slotId, buffer.getProtocolId()), buffer.readSlot());
        }
        return true;
    }

    @Override
    public void log() {
        if (slots.size() == 1) {
            Map.Entry<InventorySlots.EntityInventory, Slot> set = slots.entrySet().iterator().next();
            if (set.getValue() == null) {
                Log.protocol(String.format("Entity equipment changed (entityId=%d, slot=%s): AIR", entityId, set.getKey()));
                return;
            }
            Log.protocol(String.format("Entity equipment changed (entityId=%d, slot=%s): %dx %s", entityId, set.getKey(), set.getValue().getItemCount(), set.getValue().getDisplayName()));
        } else {
            Log.protocol(String.format("Entity equipment changed (entityId=%d, slotCount=%d)", entityId, slots.size()));
        }
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public int getEntityId() {
        return entityId;
    }

    public HashMap<InventorySlots.EntityInventory, Slot> getSlots() {
        return slots;
    }
}
