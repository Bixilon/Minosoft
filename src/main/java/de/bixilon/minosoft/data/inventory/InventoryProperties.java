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

package de.bixilon.minosoft.data.inventory;

import de.bixilon.minosoft.data.text.ChatComponent;

public class InventoryProperties {
    final int windowId;
    final InventoryTypes type;
    final ChatComponent title;
    final byte slotCount;

    public InventoryProperties(int windowId, InventoryTypes type, ChatComponent title, byte slotCount) {
        this.windowId = windowId;
        this.type = type;
        this.title = title;
        this.slotCount = slotCount;
    }

    public int getWindowId() {
        return this.windowId;
    }

    public InventoryTypes getType() {
        return this.type;
    }

    public ChatComponent getTitle() {
        return this.title;
    }

    public byte getSlotCount() {
        return this.slotCount;
    }
}
