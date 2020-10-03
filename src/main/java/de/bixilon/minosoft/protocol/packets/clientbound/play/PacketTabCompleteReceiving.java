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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketTabCompleteReceiving implements ClientboundPacket {
    int count;
    String[] match;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getProtocolId() < 37) {
            count = buffer.readVarInt();
            match = new String[]{buffer.readString()};
            return true;
        }
        if (buffer.getProtocolId() < 343) {
            count = buffer.readVarInt();
            match = new String[count];
            for (int i = 0; i < count; i++) {
                match[i] = buffer.readString();
            }
            return true;
        }
        // ToDo
        return false;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received tab complete for message(count=%d)", count));
    }

    public int getCount() {
        return count;
    }

    public String[] getMatch() {
        return match;
    }
}
