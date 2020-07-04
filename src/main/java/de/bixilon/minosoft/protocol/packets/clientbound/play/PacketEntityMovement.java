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


public class PacketEntityMovement implements ClientboundPacket {
    int entityId;
    RelativeLocation location;
    boolean onGround;

    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                this.entityId = buffer.readInt();
                this.location = new RelativeLocation(buffer.readFixedPointNumberByte(), buffer.readFixedPointNumberByte(), buffer.readFixedPointNumberByte());
                return true;
            case VERSION_1_8:
                this.entityId = buffer.readVarInt();
                this.location = new RelativeLocation(buffer.readFixedPointNumberByte(), buffer.readFixedPointNumberByte(), buffer.readFixedPointNumberByte());
                this.onGround = buffer.readBoolean();
                return true;
            case VERSION_1_9_4:
                this.entityId = buffer.readVarInt();
                this.location = new RelativeLocation(buffer.readShort() / 4096F, buffer.readShort() / 4096F, buffer.readShort() / 4096F); // / 128 / 32
                this.onGround = buffer.readBoolean();
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Entity %d moved relative %s", entityId, location.toString()));
    }

    public int getEntityId() {
        return entityId;
    }

    public RelativeLocation getRelativeLocation() {
        return location;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
