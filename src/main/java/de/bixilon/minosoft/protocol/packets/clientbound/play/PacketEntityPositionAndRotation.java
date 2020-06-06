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

import de.bixilon.minosoft.game.datatypes.entities.RelativeLocation;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;


public class PacketEntityPositionAndRotation implements ClientboundPacket {
    int entityId;
    RelativeLocation location;
    int yaw;
    int pitch;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                this.entityId = buffer.readInteger();
                this.location = new RelativeLocation(buffer.readFixedPointNumberByte(), buffer.readFixedPointNumberByte(), buffer.readFixedPointNumberByte());
                this.yaw = buffer.readByte(); //ToDo
                this.pitch = buffer.readByte();
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Entity %d moved relative %s %s %s - %s %s", entityId, location.getX(), location.getY(), location.getZ(), yaw, pitch));
    }

    public int getEntityId() {
        return entityId;
    }

    public RelativeLocation getRelativeLocation() {
        return location;
    }

    public int getYaw() {
        return yaw;
    }

    public int getPitch() {
        return pitch;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
