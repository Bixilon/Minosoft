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

import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W33A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_17W45A;

public class PacketTabCompleteReceiving extends PlayClientboundPacket {
    private final int count;
    private final String[] match;

    public PacketTabCompleteReceiving(PlayInByteBuffer buffer) {
        if (buffer.getVersionId() < V_14W33A) {
            this.count = buffer.readVarInt();
            this.match = new String[]{buffer.readString()};
            return;
        }
        if (buffer.getVersionId() < V_17W45A) {
            this.count = buffer.readVarInt();
            this.match = new String[this.count];
            for (int i = 0; i < this.count; i++) {
                this.match[i] = buffer.readString();
            }
            return;
        }
        // ToDo
        throw new IllegalStateException("TODO");
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received tab complete for message(count=%d)", this.count));
    }

    public int getCount() {
        return this.count;
    }

    public String[] getMatch() {
        return this.match;
    }
}
