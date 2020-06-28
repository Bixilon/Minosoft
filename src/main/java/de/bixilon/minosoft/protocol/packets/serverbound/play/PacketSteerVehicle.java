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

public class PacketSteerVehicle implements ServerboundPacket {

    final float sideways;
    final float forward;
    final boolean jump;
    final boolean unmount;

    public PacketSteerVehicle(float sideways, float forward, boolean jump, boolean unmount) {
        this.sideways = sideways;
        this.forward = forward;
        this.jump = jump;
        this.unmount = unmount;
        log();
    }


    @Override
    public OutPacketBuffer write(ProtocolVersion version) {
        OutPacketBuffer buffer = new OutPacketBuffer(version, version.getPacketCommand(Packets.Serverbound.PLAY_STEER_VEHICLE));
        switch (version) {
            case VERSION_1_7_10:
                buffer.writeFloat(sideways);
                buffer.writeFloat(forward);
                buffer.writeBoolean(jump);
                buffer.writeBoolean(unmount);
                break;
            case VERSION_1_8:
            case VERSION_1_9_4:
                buffer.writeFloat(sideways);
                buffer.writeFloat(forward);
                byte flags = 0;
                if (jump) {
                    flags |= 0x1;
                }
                if (unmount) {
                    flags |= 0x2;
                }
                buffer.writeByte(flags);
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Steering vehicle: %s %s (jump=%s, unmount=%s)", sideways, forward, jump, unmount));
    }
}
