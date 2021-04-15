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

package de.bixilon.minosoft.protocol.packets.c2s.play;

import de.bixilon.minosoft.data.inventory.InventoryActions;
import de.bixilon.minosoft.data.inventory.ItemStack;
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket;
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class ClickWindowSlotC2SPacket implements PlayC2SPacket {
    private final byte windowId;
    private final short slot;
    private final InventoryActions action;
    private final short actionNumber;
    private final ItemStack clickedItem;

    public ClickWindowSlotC2SPacket(byte windowId, short slot, InventoryActions action, short actionNumber, ItemStack clickedItem) {
        this.windowId = windowId;
        this.slot = slot;
        this.action = action;
        this.actionNumber = actionNumber;
        this.clickedItem = clickedItem;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        buffer.writeByte(this.windowId);
        buffer.writeShort(this.slot);
        buffer.writeByte(this.action.getButton());
        buffer.writeShort(this.actionNumber);
        buffer.writeByte(this.action.getMode());
        buffer.writeItemStack(this.clickedItem);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Clicking in window (windowId=%d, slot=%d, action=%s)", this.windowId, this.slot, this.action));
    }
}
