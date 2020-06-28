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
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

public class PacketUpdateHealth implements ClientboundPacket {
    float health;
    int food;
    float saturation;


    @Override
    public void read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                health = buffer.readFloat();
                food = buffer.readShort();
                saturation = buffer.readFloat();
                break;
            case VERSION_1_8:
            case VERSION_1_9_4:
                health = buffer.readFloat();
                food = buffer.readVarInt();
                saturation = buffer.readFloat();
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Health update. Now at %s hearts and %s food level and %s saturation", (Math.round(health * 10) / 10.0), (Math.round(food * 10) / 10.0), saturation));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public int getFood() {
        return food;
    }

    public float getHealth() {
        return health;
    }

    public float getSaturation() {
        return saturation;
    }
}
