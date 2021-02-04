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

import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.modding.event.events.SingleSlotChangeEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketSetSlot extends ClientboundPacket {
    byte windowId;
    short slotId;
    Slot slot; // ToDo use enum Slots

    @Override
    public boolean read(InByteBuffer buffer) {
        this.windowId = buffer.readByte();
        this.slotId = buffer.readShort();
        this.slot = buffer.readSlot();
        return true;
    }

    @Override
    public void handle(Connection connection) {
        connection.fireEvent(new SingleSlotChangeEvent(connection, this));

        if (getWindowId() == -1) {
            // thanks mojang
            // ToDo: what is windowId -1
            return;
        }
        connection.getPlayer().setSlot(getWindowId(), getSlotId(), getSlot());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received slot data (windowId=%d, slotId=%d, item=%s)", this.windowId, this.slotId, ((this.slot == null) ? "AIR" : this.slot.getDisplayName())));
    }

    public byte getWindowId() {
        return this.windowId;
    }

    public short getSlotId() {
        return this.slotId;
    }

    public Slot getSlot() {
        return this.slot;
    }
}
