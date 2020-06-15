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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketPlayerAbilitiesSending implements ServerboundPacket {
    boolean creative;
    boolean flying;
    boolean canFly;
    boolean godMode;
    float flyingSpeed;
    float walkingSpeed;

    public PacketPlayerAbilitiesSending(boolean creative, boolean flying, boolean canFly, boolean godMode, float flyingSpeed, float walkingSpeed) {
        this.creative = creative;
        this.flying = flying;
        this.canFly = canFly;
        this.godMode = godMode;
        this.flyingSpeed = flyingSpeed;
        this.walkingSpeed = walkingSpeed;
        log();
    }

    public PacketPlayerAbilitiesSending(boolean flying) {
        this.creative = false;
        this.flying = flying;
        this.canFly = flying;
        this.godMode = false;
        this.flyingSpeed = 0.05F;
        this.walkingSpeed = 0.1F;

    }


    @Override
    public OutPacketBuffer write(ProtocolVersion v) {
        OutPacketBuffer buffer = new OutPacketBuffer(v.getPacketCommand(Packets.Serverbound.PLAY_CLIENT_SETTINGS));
        switch (v) {
            case VERSION_1_7_10:
                byte flags = 0;
                if (creative) {
                    flags |= 0b1;
                }
                if (flying) {
                    flags |= 0b10;
                }
                if (canFly) {
                    flags |= 0b100;
                }
                if (godMode) {
                    flags |= 0b1000;
                }
                buffer.writeByte(flags);
                buffer.writeFloat(flyingSpeed);
                buffer.writeFloat(walkingSpeed);
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending player abilities packet: (creative=%s, flying=%s, canFly=%s, godMode=%s, flyingSpeed=%s, walkingSpeed=%s)", creative, flying, canFly, godMode, flyingSpeed, walkingSpeed));
    }
}
