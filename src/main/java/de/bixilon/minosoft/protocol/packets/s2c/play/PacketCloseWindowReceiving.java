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

package de.bixilon.minosoft.protocol.packets.s2c.play;

import de.bixilon.minosoft.modding.event.events.ContainerCloseEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketCloseWindowReceiving extends PlayS2CPacket {
    private final int windowId;

    public PacketCloseWindowReceiving(PlayInByteBuffer buffer) {
        this.windowId = buffer.readByte();
    }

    @Override
    public void handle(PlayConnection connection) {
        ContainerCloseEvent event = new ContainerCloseEvent(connection, this);
        if (connection.fireEvent(event)) {
            return;
        }

        connection.getPlayer().getInventoryManager().getInventories().remove(getWindowId());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Closing inventory (windowId=%d)", this.windowId));
    }

    public int getWindowId() {
        return this.windowId;
    }
}
