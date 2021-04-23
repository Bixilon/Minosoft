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

package de.bixilon.minosoft.protocol.packets.s2c.login;

import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.protocol.ConnectionStates;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.logging.Log;

import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W12A;

public class PacketLoginSuccess extends PlayS2CPacket {
    private final UUID uuid;
    private final String playerName;

    public PacketLoginSuccess(PlayInByteBuffer buffer) {
        if (buffer.getVersionId() < V_20W12A) {
            this.uuid = Util.getUUIDFromString(buffer.readString());
        } else {
            this.uuid = buffer.readUUID();
        }
        this.playerName = buffer.readString();
    }

    @Override
    public void handle(PlayConnection connection) {
        connection.setConnectionState(ConnectionStates.PLAY);

        var playerEntity = connection.getPlayer().getEntity();
        playerEntity.getTabListItem().setName(this.playerName);
        playerEntity.getTabListItem().setDisplayName(ChatComponent.Companion.valueOf(this.playerName));

        connection.getWorld().getEntities().add(null, this.uuid, playerEntity);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Receiving login success packet (username=%s, uuid=%s)", this.playerName, this.uuid));
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getPlayerName() {
        return this.playerName;
    }
}
