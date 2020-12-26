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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.data.inventory.InventoryActions;
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

public class PacketClickWindow implements ServerboundPacket {

    final byte windowId;
    final short slot;
    final InventoryActions action;
    final short actionNumber;
    final Slot clickedItem;

    public PacketClickWindow(byte windowId, short slot, InventoryActions action, short actionNumber, Slot clickedItem) {
        this.windowId = windowId;
        this.slot = slot;
        this.action = action;
        this.actionNumber = actionNumber;
        this.clickedItem = clickedItem;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_CLICK_WINDOW);
        buffer.writeByte(this.windowId);
        buffer.writeShort(this.slot);
        buffer.writeByte(this.action.getButton());
        buffer.writeShort(this.actionNumber);
        buffer.writeByte(this.action.getMode());
        buffer.writeSlot(this.clickedItem);
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Clicking in window (windowId=%d, slot=%d, action=%s)", this.windowId, this.slot, this.action));
    }
}
