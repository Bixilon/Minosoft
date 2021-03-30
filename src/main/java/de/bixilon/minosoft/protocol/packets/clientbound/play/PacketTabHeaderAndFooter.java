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

import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.modding.event.events.PlayerListInfoChangeEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketTabHeaderAndFooter extends ClientboundPacket {
    private final ChatComponent header;
    private final ChatComponent footer;

    public PacketTabHeaderAndFooter(InByteBuffer buffer) {
        this.header = buffer.readChatComponent();
        this.footer = buffer.readChatComponent();
    }

    @Override
    public void handle(Connection connection) {
        if (connection.fireEvent(new PlayerListInfoChangeEvent(connection, this))) {
            return;
        }

        connection.getPlayer().setTabHeader(getHeader());
        connection.getPlayer().setTabFooter(getFooter());
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received tab list header: %s", this.header.getANSIColoredMessage()));
        Log.protocol(String.format("[IN] Received tab list footer: %s", this.footer.getANSIColoredMessage()));
    }

    public ChatComponent getHeader() {
        return this.header;
    }

    public ChatComponent getFooter() {
        return this.footer;
    }
}
