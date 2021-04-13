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

import de.bixilon.minosoft.data.inventory.ItemStack;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketWindowItems;

public class MultiSlotChangeEvent extends PlayConnectionEvent {
    private final byte windowId;
    private final ItemStack[] data;

    public MultiSlotChangeEvent(PlayConnection connection, byte windowId, ItemStack[] data) {
        super(connection);
        this.windowId = windowId;
        this.data = data;
    }

    public MultiSlotChangeEvent(PlayConnection connection, PacketWindowItems pkg) {
        super(connection);
        this.windowId = pkg.getWindowId();
        this.data = pkg.getData();
    }

    public byte getWindowId() {
        return this.windowId;
    }

    /**
     * @return Data array. Array position equals the slot id
     */
    public ItemStack[] getData() {
        return this.data;
    }
}
