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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.modding.event.events.DisconnectEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

public class PacketDisconnect extends PlayClientboundPacket {
    private final ChatComponent reason;

    public PacketDisconnect(PlayInByteBuffer buffer) {
        this.reason = buffer.readChatComponent();
    }

    @Override
    public void handle(PlayConnection connection) {
        connection.fireEvent(new DisconnectEvent(connection, this));
        // got kicked
        connection.disconnect();
        Log.info(String.format("Kicked from %s (reason=%s)", connection.getAddress(), getReason().getAnsiColoredMessage()));
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received disconnect packet (reason=\"%s\")", this.reason.getAnsiColoredMessage()));
    }

    public ChatComponent getReason() {
        return this.reason;
    }
}
