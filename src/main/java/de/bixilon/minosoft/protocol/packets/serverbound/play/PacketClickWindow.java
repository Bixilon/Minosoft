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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.game.datatypes.inventory.InventoryAction;
import de.bixilon.minosoft.game.datatypes.inventory.Slot;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketClickWindow implements ServerboundPacket {

    final byte windowId;
    final short slot;
    final InventoryAction action;
    final short actionNumber;
    final Slot clickedItem;

    public PacketClickWindow(byte windowId, short slot, InventoryAction action, short actionNumber, Slot clickedItem) {
        this.windowId = windowId;
        this.slot = slot;
        this.action = action;
        this.actionNumber = actionNumber;
        this.clickedItem = clickedItem;
    }


    @Override
    public OutPacketBuffer write(ProtocolVersion version) {
        OutPacketBuffer buffer = new OutPacketBuffer(version, version.getPacketCommand(Packets.Serverbound.PLAY_CLICK_WINDOW));
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                buffer.writeByte(windowId);
                buffer.writeShort(slot);
                buffer.writeByte(action.getButton());
                buffer.writeShort(actionNumber);
                buffer.writeByte(action.getMode());
                buffer.writeSlot(clickedItem);
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Clicking in window (windowId=%d, slot=%d, action=%s)", windowId, slot, action));
    }

}
