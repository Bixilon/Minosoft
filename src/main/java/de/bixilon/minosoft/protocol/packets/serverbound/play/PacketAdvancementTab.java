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

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

public class PacketAdvancementTab implements ServerboundPacket {
    final AdvancementTabStatus action;
    final String tabToOpen;

    public PacketAdvancementTab(AdvancementTabStatus action) {
        this.action = action;
        tabToOpen = null;
    }

    public PacketAdvancementTab(AdvancementTabStatus action, String tabToOpen) {
        this.action = action;
        this.tabToOpen = tabToOpen;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_ADVANCEMENT_TAB);
        buffer.writeVarInt(action.getId());
        if (action == AdvancementTabStatus.OPEN_TAB) {
            buffer.writeString(tabToOpen);
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending advancement tab packet (action=%s, tabToOpen=%s)", action, tabToOpen));
    }

    public enum AdvancementTabStatus {
        OPEN_TAB(0),
        CLOSE_TAB(1);

        final int id;

        AdvancementTabStatus(int id) {
            this.id = id;
        }

        public static AdvancementTabStatus byId(int id) {
            for (AdvancementTabStatus action : values()) {
                if (action.getId() == id) {
                    return action;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}
