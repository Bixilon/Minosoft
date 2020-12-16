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
import de.bixilon.minosoft.data.inventory.InventorySlots;
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.modding.event.events.EntityEquipmentChangeEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

import java.util.HashMap;
import java.util.Map;

import static de.bixilon.minosoft.protocol.protocol.Versions.V_15W31A;
import static de.bixilon.minosoft.protocol.protocol.Versions.V_1_16_PRE7;

public class PacketEntityEquipment extends ClientboundPacket {
    final HashMap<InventorySlots.EntityInventorySlots, Slot> slots = new HashMap<>();
    int entityId;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.entityId = buffer.readEntityId();
        if (buffer.getVersionId() < V_15W31A) {
            this.slots.put(InventorySlots.EntityInventorySlots.byId(buffer.readShort(), buffer.getVersionId()), buffer.readSlot());
            return true;
        }
        if (buffer.getVersionId() < V_1_16_PRE7) {
            this.slots.put(InventorySlots.EntityInventorySlots.byId(buffer.readVarInt(), buffer.getVersionId()), buffer.readSlot());
            return true;
        }
        boolean slotAvailable = true;
        while (slotAvailable) {
            int slotId = buffer.readByte();
            if (slotId >= 0) {
                slotAvailable = false;
            }
            slotId &= 0x7F;
            this.slots.put(InventorySlots.EntityInventorySlots.byId(slotId, buffer.getVersionId()), buffer.readSlot());
        }
        return true;
    }

    @Override
    public void handle(Connection connection) {
        connection.fireEvent(new EntityEquipmentChangeEvent(connection, this));

        Entity entity = connection.getPlayer().getWorld().getEntity(getEntityId());
        if (entity == null) {
            // thanks mojang
            return;
        }
        entity.setEquipment(getSlots());
    }

    @Override
    public void log() {
        if (this.slots.size() == 1) {
            Map.Entry<InventorySlots.EntityInventorySlots, Slot> set = this.slots.entrySet().iterator().next();
            if (set.getValue() == null) {
                Log.protocol(String.format("[IN] Entity equipment changed (entityId=%d, slot=%s): AIR", this.entityId, set.getKey()));
                return;
            }
            Log.protocol(String.format("[IN] Entity equipment changed (entityId=%d, slot=%s, item=%s): %dx %s", this.entityId, set.getKey(), set.getValue().getItem(), set.getValue().getItemCount(), set.getValue().getDisplayName()));
        } else {
            Log.protocol(String.format("[IN] Entity equipment changed (entityId=%d, slotCount=%d)", this.entityId, this.slots.size()));
        }
    }

    public int getEntityId() {
        return this.entityId;
    }

    public HashMap<InventorySlots.EntityInventorySlots, Slot> getSlots() {
        return this.slots;
    }
}
