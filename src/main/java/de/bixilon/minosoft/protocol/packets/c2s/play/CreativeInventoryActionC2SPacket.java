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

import de.bixilon.minosoft.data.inventory.ItemStack;
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket;
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class CreativeInventoryActionC2SPacket implements PlayC2SPacket {
    private final short slot;
    private final ItemStack clickedItem;

    public CreativeInventoryActionC2SPacket(short slot, ItemStack clickedItem) {
        this.slot = slot;
        this.clickedItem = clickedItem;
    }

    @Override
    public void write(PlayOutByteBuffer buffer) {
        buffer.writeShort(this.slot);
        buffer.writeItemStack(this.clickedItem);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending creative inventory action (slot=%d, item=%s)", this.slot, this.clickedItem.getDisplayName()));
    }
}
