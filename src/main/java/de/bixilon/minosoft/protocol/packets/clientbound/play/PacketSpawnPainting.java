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

import de.bixilon.minosoft.game.datatypes.entities.objects.Painting;
import de.bixilon.minosoft.game.datatypes.objectLoader.motives.Motive;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;

import java.util.UUID;

public class PacketSpawnPainting implements ClientboundPacket {
    Painting entity;

    @Override
    public boolean read(InByteBuffer buffer) {
        int entityId = buffer.readVarInt();
        UUID uuid = null;
        if (buffer.getProtocolId() >= 95) {
            uuid = buffer.readUUID();
        }
        Motive motive;
        if (buffer.getProtocolId() < 353) {
            motive = buffer.getConnection().getMapping().getMotiveByIdentifier(buffer.readString());
        } else {
            motive = buffer.getConnection().getMapping().getMotiveById(buffer.getProtocolId());
        }
        BlockPosition position;
        if (buffer.getProtocolId() < 7) {
            position = buffer.readBlockPositionInteger();
        } else {
            position = buffer.readPosition();
        }
        int direction;
        if (buffer.getProtocolId() < 8) {
            direction = buffer.readInt();
        } else {
            direction = buffer.readByte();
        }
        entity = new Painting(entityId, uuid, motive, position, direction);
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Spawning painting at %s (entityId=%d, motive=%s, direction=%d)", entity.getLocation(), entity.getEntityId(), entity.getMotive(), entity.getDirection()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public Painting getEntity() {
        return entity;
    }
}

