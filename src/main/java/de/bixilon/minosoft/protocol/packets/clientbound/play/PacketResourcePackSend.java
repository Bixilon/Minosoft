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
import de.bixilon.minosoft.modding.event.events.ResourcePackChangeEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W45A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_21W15A;

public class PacketResourcePackSend extends PlayClientboundPacket {
    private final String url;
    private final String hash;
    private boolean forced;
    private ChatComponent promptText;

    public PacketResourcePackSend(PlayInByteBuffer buffer) {
        this.url = buffer.readString();
        Util.checkURL(this.url);
        this.hash = buffer.readString();
        if (buffer.getVersionId() >= V_20W45A) {
            this.forced = buffer.readBoolean();
        }
        if (buffer.getVersionId() >= V_21W15A) {
            if (buffer.readBoolean()) {
                this.promptText = buffer.readChatComponent();
            }
        }
    }

    @Override
    public void handle(PlayConnection connection) {
        ResourcePackChangeEvent event = new ResourcePackChangeEvent(connection, this);
        if (connection.fireEvent(event)) {
            return;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received resource pack send (url=\"%s\", hash=%s)", this.url, this.hash));
    }

    public String getUrl() {
        return this.url;
    }

    public String getHash() {
        return this.hash;
    }

    public boolean isForced() {
        return this.forced;
    }

    public ChatComponent getPromptText() {
        return this.promptText;
    }
}
