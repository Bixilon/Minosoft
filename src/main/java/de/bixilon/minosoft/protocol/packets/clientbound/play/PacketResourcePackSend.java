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

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.modding.event.events.ResourcePackChangeEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

import static de.bixilon.minosoft.protocol.protocol.Versions.V_20W45A;

public class PacketResourcePackSend extends ClientboundPacket {
    String url;
    String hash;
    boolean forced;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.url = buffer.readString();
        this.hash = buffer.readString();
        if (buffer.getVersionId() >= V_20W45A) {
            this.forced = buffer.readBoolean();
        }
        return true;
    }

    @Override
    public void handle(Connection connection) {
        ResourcePackChangeEvent event = new ResourcePackChangeEvent(connection, this);
        if (connection.fireEvent(event)) {
            return;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received resource pack send (url=\"%s\", hash=%s", this.url, this.hash));
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
}
