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
import de.bixilon.minosoft.util.BitByte;

public class PacketStopSound implements ClientboundPacket {
    Integer soundId;
    String soundIdentifier;


    @Override
    public boolean read(InByteBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
                buffer.readString(); // category
                soundIdentifier = buffer.readString();
                return true;
            case VERSION_1_12_2:
                soundIdentifier = buffer.readString();
                buffer.readString(); // category
                return true;
            case VERSION_1_13_2:
                byte flags = buffer.readByte();
                if (BitByte.isBitMask(flags, 0x01)) {
                    soundId = buffer.readVarInt();
                }
                if (BitByte.isBitMask(flags, 0x02)) {
                    soundIdentifier = buffer.readString();
                }
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received stop sound (soundId=%d, soundIdentifier=%s)", soundId, soundIdentifier));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public Integer getSoundId() {
        return soundId;
    }

    public String getSoundIdentifier() {
        return soundIdentifier;
    }
}
