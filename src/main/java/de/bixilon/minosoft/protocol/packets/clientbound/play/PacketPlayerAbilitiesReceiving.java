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
import de.bixilon.minosoft.util.BitByte;

public class PacketPlayerAbilitiesReceiving implements ClientboundPacket {
    boolean creative; // is this needed? receiving the gameMode in change Gamestate
    boolean flying;
    boolean canFly;
    boolean godMode;
    float flyingSpeed;
    float walkingSpeed;


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10: {
                byte flags = buffer.readByte();
                creative = BitByte.isBitSet(flags, 0);
                flying = BitByte.isBitSet(flags, 1);
                canFly = BitByte.isBitSet(flags, 2);
                godMode = BitByte.isBitSet(flags, 3);
                flyingSpeed = buffer.readFloat();
                walkingSpeed = buffer.readFloat();
                return true;
            }
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10: {
                byte flags = buffer.readByte();
                godMode = BitByte.isBitSet(flags, 0);
                flying = BitByte.isBitSet(flags, 1);
                canFly = BitByte.isBitSet(flags, 2);
                creative = BitByte.isBitSet(flags, 3);
                flyingSpeed = buffer.readFloat();
                walkingSpeed = buffer.readFloat();
                return true;
            }
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Received player abilities packet: (creative=%s, flying=%s, canFly=%s, godMode=%s, flyingSpeed=%s, walkingSpeed=%s)", creative, flying, canFly, godMode, flyingSpeed, walkingSpeed));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public boolean canFly() {
        return canFly;
    }

    public boolean isCreative() {
        return creative;
    }

    public boolean isGodMode() {
        return godMode;
    }

    public boolean isFlying() {
        return flying;
    }
}
