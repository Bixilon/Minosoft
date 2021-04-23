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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.inventory.InventorySlots;
import de.bixilon.minosoft.data.inventory.ItemStack;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.play.PacketEntityEquipment;

import java.util.HashMap;

public class EntityEquipmentChangeEvent extends PlayConnectionEvent {
    private final int entityId;
    private final HashMap<InventorySlots.EquipmentSlots, ItemStack> slots;

    public EntityEquipmentChangeEvent(PlayConnection connection, int entityId, HashMap<InventorySlots.EquipmentSlots, ItemStack> slots) {
        super(connection);
        this.entityId = entityId;
        this.slots = slots;
    }

    public EntityEquipmentChangeEvent(PlayConnection connection, PacketEntityEquipment pkg) {
        super(connection);
        this.entityId = pkg.getEntityId();
        this.slots = pkg.getSlots();
    }

    public Entity getEntity() {
        return getConnection().getWorld().getEntities().get(this.entityId);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public HashMap<InventorySlots.EquipmentSlots, ItemStack> getSlots() {
        return this.slots;
    }
}
