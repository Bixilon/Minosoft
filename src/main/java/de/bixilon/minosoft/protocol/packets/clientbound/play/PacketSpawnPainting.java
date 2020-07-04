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

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.util.UUID;

public class PacketSpawnPainting implements ClientboundPacket {
    int entityId;
    String title;
    BlockPosition position;
    int direction;

    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10:
                entityId = buffer.readVarInt();
                title = buffer.readString();
                position = buffer.readBlockPositionInteger();
                direction = buffer.readInt();
                return true;
            case VERSION_1_8:
                entityId = buffer.readVarInt();
                title = buffer.readString();
                position = buffer.readPosition();
                direction = buffer.readByte();
                return true;
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
                entityId = buffer.readVarInt();
                UUID uuid = buffer.readUUID(); // ?
                title = buffer.readString();
                position = buffer.readPosition();
                direction = buffer.readByte();
                return true;
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Spawning painting at %s (entityId=%d, title=%s, direction=%d)", position.toString(), entityId, position, direction));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public int getEntityId() {
        return entityId;
    }

    public String getTitle() {
        return title;
    }

    public BlockPosition getPosition() {
        return position;
    }

    public int getDirection() {
        return direction;
    }
}
